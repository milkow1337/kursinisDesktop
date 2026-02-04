package com.example.courseprifs.fxControllers;

import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ChatForm implements Initializable {

    @FXML public ListView<Review> messageList;
    @FXML public TextArea messageBody;
    @FXML public Button sendButton;
    @FXML public Label chatStatusLabel;
    @FXML public VBox chatContainer;

    private EntityManagerFactory entityManagerFactory;
    private CustomHibernate customHibernate;
    private User currentUser;
    private FoodOrder currentFoodOrder;
    private Chat currentChat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (chatStatusLabel != null) {
            chatStatusLabel.setText("");
        }
    }

    public void setData(EntityManagerFactory entityManagerFactory, User currentUser, FoodOrder currentFoodOrder) {
        this.entityManagerFactory = entityManagerFactory;
        this.currentUser = currentUser;
        this.currentFoodOrder = currentFoodOrder;
        this.customHibernate = new CustomHibernate(entityManagerFactory);

        loadOrCreateChat();
        loadMessages();
        checkChatLock();
    }

    private void loadOrCreateChat() {
        FoodOrder order = customHibernate.getEntityById(FoodOrder.class, currentFoodOrder.getId());

        if (order.getChat() == null) {
            Chat chat = new Chat(
                    "Order Chat #" + order.getId(),
                    order
            );
            customHibernate.create(chat);

            order = customHibernate.getEntityById(FoodOrder.class, currentFoodOrder.getId());
        }

        this.currentChat = order.getChat();
        this.currentFoodOrder = order;
    }

    private void loadMessages() {
        if (currentChat == null) {
            return;
        }

        Chat freshChat = customHibernate.getEntityById(Chat.class, currentChat.getId());
        messageList.getItems().clear();

        if (freshChat.getMessages() != null && !freshChat.getMessages().isEmpty()) {
            messageList.getItems().addAll(freshChat.getMessages());
        }
    }

    private void checkChatLock() {
        boolean isLocked = customHibernate.isChatLocked(currentChat);

        if (isLocked) {
            // Disable sending messages if order is completed
            messageBody.setDisable(true);
            sendButton.setDisable(true);
            chatStatusLabel.setText("⚠️ Chat is locked - Order completed");
            chatStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            checkUserPermissions();
        }
    }

    private void checkUserPermissions() {
        boolean canParticipate = false;

        if (currentUser instanceof BasicUser) {
            BasicUser basicUser = (BasicUser) currentUser;
            canParticipate = currentFoodOrder.getBuyer() != null &&
                    currentFoodOrder.getBuyer().getId() == basicUser.getId();
        } else if (currentUser instanceof Restaurant) {
            Restaurant restaurant = (Restaurant) currentUser;
            canParticipate = currentFoodOrder.getRestaurant() != null &&
                    currentFoodOrder.getRestaurant().getId() == restaurant.getId();
        } else if (currentUser instanceof Driver) {
            Driver driver = (Driver) currentUser;
            canParticipate = currentFoodOrder.getDriver() != null &&
                    currentFoodOrder.getDriver().getId() == driver.getId();
        } else if (currentUser.isAdmin()) {
            messageBody.setDisable(true);
            sendButton.setDisable(true);
            chatStatusLabel.setText("ℹ️ Admin mode - Read only");
            chatStatusLabel.setStyle("-fx-text-fill: blue;");
            return;
        }

        if (!canParticipate) {
            messageBody.setDisable(true);
            sendButton.setDisable(true);
            chatStatusLabel.setText("⚠️ You cannot participate in this chat");
            chatStatusLabel.setStyle("-fx-text-fill: orange;");
        } else {
            chatStatusLabel.setText("✓ Chat active");
            chatStatusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    public void sendMessage() {
        String messageText = messageBody.getText().trim();

        if (messageText.isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Empty Message",
                    "Please enter a message before sending.");
            return;
        }

        try {
            // new message
            Review message = new Review(
                    messageText,
                    (BasicUser) currentUser,
                    currentChat
            );
            message.setDateCreated(LocalDate.now());

            customHibernate.create(message);

            messageBody.clear();
            loadMessages();

            messageList.scrollTo(messageList.getItems().size() - 1);

        } catch (ClassCastException e) {
            FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "Permission Denied",
                    "Only customers, drivers, and restaurants can send messages.");
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error sending message", e);
        }
    }

    @FXML
    public void refreshMessages() {
        loadMessages();
        checkChatLock();
    }

    @FXML
    public void closeChat() {
        if (messageList != null && messageList.getScene() != null) {
            messageList.getScene().getWindow().hide();
        }
    }
}