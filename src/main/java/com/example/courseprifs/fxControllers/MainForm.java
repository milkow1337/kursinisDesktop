package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainForm implements Initializable {
    @FXML
    public Tab userTab;
    @FXML
    public Tab managementTab;
    @FXML
    public Tab foodTab;
    @FXML
    public ListView<User> userListField;
    @FXML //Laikinas, galutineje versijoje jo nebus
    public Tab altTab;
    @FXML
    public TabPane tabsPane;
    //<editor-fold desc="User Tab Elements">
    @FXML
    public TableView<UserTableParameters> userTable;
    @FXML
    public TableColumn<UserTableParameters, Integer> idCol;
    @FXML
    public TableColumn<UserTableParameters, String> userTypeCol;
    @FXML
    public TableColumn<UserTableParameters, String> loginCol;
    @FXML
    public TableColumn<UserTableParameters, String> passCol;
    @FXML
    public TableColumn<UserTableParameters, String> nameCol;
    @FXML
    public TableColumn<UserTableParameters, String> surnameCol;
    @FXML
    public TableColumn<UserTableParameters, String> addrCol;
    @FXML
    public TableColumn<UserTableParameters, Void> dummyCol;

    private ObservableList<UserTableParameters> data = FXCollections.observableArrayList();

    //</editor-fold>
    //<editor-fold desc="Order Tab Elements">
    public ListView<FoodOrder> ordersList;
    public TextField titleField;
    public ComboBox<BasicUser> clientList;
    public TextField priceField;
    public ComboBox<Restaurant> restaurantField;
    public ListView<BasicUser> basicUserList;
    public ComboBox<OrderStatus> orderStatusField;
    public ComboBox<OrderStatus> filterStatus;
    public ComboBox<BasicUser> filterClients;
    public DatePicker filterFrom;
    public DatePicker filterTo;
    public ListView<Cuisine> foodList;
    //</editor-fold>
    //<editor-fold desc="Cuisine Tab Elements">
    public TextField titleCuisineField;
    public TextArea ingredientsField;
    public ListView<Restaurant> restaurantList;
    public TextField cuisinePriceField;
    public CheckBox isDeadly;
    public CheckBox isVegan;
    public ListView<Cuisine> cuisineList;
    //</editor-fold>

    //<editor-fold desc="Admin chat Elements">
    public Tab chatTab;
    public ListView<Chat> allChats;
    public ListView<Review> chatMessages;
    //</editor-fold>


    private EntityManagerFactory entityManagerFactory;
    private CustomHibernate customHibernate;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTable.setEditable(true);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        passCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setPassword(event.getNewValue());
            User user = customHibernate.getEntityById(User.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setPassword(event.getNewValue());
            customHibernate.update(user);
        });

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());
            User user = customHibernate.getEntityById(User.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setName(event.getNewValue());
            customHibernate.update(user);
        });

        //pabaigti likusius stulpelius
//        addrCol.setCellValueFactory(new PropertyValueFactory<>(""));
    }

    public void setData(EntityManagerFactory entityManagerFactory, User user) {
        this.entityManagerFactory = entityManagerFactory;
        this.currentUser = user;
        this.customHibernate = new CustomHibernate(entityManagerFactory);
        setUserFormVisibility();
    }

    private void setUserFormVisibility() {
        if (currentUser instanceof User) {
            //turbut nieko nedarom, gal kazka custom

        } else if (currentUser instanceof Restaurant) {
//            altTab.setDisable(true);
            tabsPane.getTabs().remove(altTab); //Man net nesugeneruos sito tabo
        }

    }

    //<editor-fold desc="User Tab functionality">
    public void reloadTableData() {
        if (userTab.isSelected()) {
            List<User> users = customHibernate.getAllRecords(User.class);
            for (User u : users) {
                UserTableParameters userTableParameters = new UserTableParameters();
                userTableParameters.setId(u.getId());
                userTableParameters.setUserType(u.getClass().getSimpleName());
                userTableParameters.setLogin(u.getLogin());
                userTableParameters.setPassword(u.getPassword());
                //baigti bendrus laukus
                if (u instanceof BasicUser) {
                    userTableParameters.setAddress(((BasicUser) u).getAddress());
                }
                if (u instanceof Restaurant) {

                }
                if (u instanceof Driver) {

                }
                data.add(userTableParameters);
            }
            userTable.getItems().addAll(data);
        } else if (managementTab.isSelected()) {
            clearAllOrderFields();
            List<FoodOrder> foodOrders = getFoodOrders();
            ordersList.getItems().addAll(foodOrders);
            //double check kodel rodo per daug vartotoju
            clientList.getItems().addAll(customHibernate.getAllRecords(BasicUser.class));
            //jei dirbsit su ListView:
            basicUserList.getItems().addAll(customHibernate.getAllRecords(BasicUser.class));
            restaurantField.getItems().addAll(customHibernate.getAllRecords(Restaurant.class));
            orderStatusField.getItems().addAll(OrderStatus.values());
        } else if (altTab.isSelected()) {
            List<User> userList = customHibernate.getAllRecords(User.class);
            userListField.getItems().addAll(userList);
        } else if (foodTab.isSelected()) {
            clearAllCuisineFields();
            restaurantList.getItems().addAll(customHibernate.getAllRecords(Restaurant.class));
        } else if (chatTab.isSelected()) {
            allChats.getItems().addAll(customHibernate.getAllRecords(Chat.class));

        }
        //pabaigt
    }

    private void clearAllOrderFields() {
        //turbut reik salygos sakiniu
        ordersList.getItems().clear();
        basicUserList.getItems().clear();
        clientList.getItems().clear();
        restaurantField.getItems().clear();
        titleField.clear();
        priceField.clear();
    }

    private void clearAllCuisineFields() {
        foodList.getItems().clear();
        cuisinePriceField.clear();
        titleCuisineField.clear();
        ingredientsField.clear();
        isDeadly.setSelected(false);
        isVegan.setSelected(false);
        restaurantList.getItems().clear();
    }
    //</editor-fold>

    //<editor-fold desc="Alternative Tab Functions">

    public void addUser(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, null, false);


        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void loadUser(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        User selectedUser = userListField.getSelectionModel().getSelectedItem();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, selectedUser, true);


        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void deleteUser() {
        User selectedUser = userListField.getSelectionModel().getSelectedItem();
        customHibernate.delete(User.class, selectedUser.getId());
    }

    //</editor-fold>

    //<editor-fold desc="Order Tab functionality">
    private List<FoodOrder> getFoodOrders() {
        if (currentUser instanceof Restaurant) {
            return customHibernate.getRestaurantOrders((Restaurant) currentUser);
        } else {
            return customHibernate.getAllRecords(FoodOrder.class);
        }
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void createOrder() {
        if (isNumeric(priceField.getText())) {
//            FoodOrder foodOrder = new FoodOrder(titleField.getText(), Double.parseDouble(priceField.getText()), clientList.getValue(), restaurantField.getValue());
            FoodOrder foodOrder = new FoodOrder(titleField.getText(), Double.parseDouble(priceField.getText()), clientList.getValue(), foodList.getSelectionModel().getSelectedItems(), restaurantField.getValue());
            customHibernate.create(foodOrder);

            //Alternatyvus būdas:
//            FoodOrder foodOrder2 = new FoodOrder(titleField.getText(), Double.parseDouble(priceField.getText()), basicUserList.getSelectionModel().getSelectedItem(), restaurantField.getValue());
//            customHibernate.create(foodOrder2);
            fillOrderLists();
        }
    }

    public void updateOrder() {
        FoodOrder foodOrder = ordersList.getSelectionModel().getSelectedItem();
        foodOrder.setRestaurant(restaurantField.getSelectionModel().getSelectedItem());
        foodOrder.setName(titleField.getText());
        foodOrder.setPrice(Double.valueOf(priceField.getText()));
        foodOrder.setOrderStatus(orderStatusField.getValue());
        foodOrder.setBuyer(clientList.getSelectionModel().getSelectedItem());

        customHibernate.update(foodOrder);
        //SItas naudojamas bus daug kur
        fillOrderLists();
    }

    public void deleteOrder() {
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        customHibernate.delete(FoodOrder.class, selectedOrder.getId());
        fillOrderLists();
    }

    private void fillOrderLists() {
        ordersList.getItems().clear();
        ordersList.getItems().addAll(customHibernate.getAllRecords(FoodOrder.class));
    }

    public void loadOrderInfo() {
        //not optimal, code duplication
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        clientList.getItems().stream()
                .filter(c -> c.getId() == selectedOrder.getBuyer().getId())
                .findFirst()
                .ifPresent(u -> clientList.getSelectionModel().select(u));

        basicUserList.getItems().stream()
                .filter(c -> c.getId() == selectedOrder.getBuyer().getId())
                .findFirst()
                .ifPresent(u -> basicUserList.getSelectionModel().select(u));
        titleField.setText(selectedOrder.getName());
        priceField.setText(selectedOrder.getPrice().toString());
        restaurantField.getItems().stream()
                .filter(r -> r.getId() == selectedOrder.getRestaurant().getId())
                .findFirst()
                .ifPresent(u -> restaurantField.getSelectionModel().select(u));
        //orderStatusField.getItems().stream() PATIEMS PABANDYT
        //greiciausiai reiktu field enable/disable
        disableFoodOrderFields();

    }

    private void disableFoodOrderFields() {
        if (orderStatusField.getSelectionModel().getSelectedItem() == OrderStatus.COMPLETED) {
            clientList.setDisable(true);
            priceField.setDisable(true);
        }
    }

    public void filterOrders() {
    }

    public void loadRestaurantMenuForOrder() {
        foodList.getItems().clear();
        foodList.getItems().addAll(customHibernate.getRestaurantCuisine(restaurantField.getSelectionModel().getSelectedItem()));
    }
    //</editor-fold>

    //<editor-fold desc="Cuisine Tab Functionality">
    public void createNewMenuItem() {
        Cuisine cuisine = new Cuisine(titleCuisineField.getText(), ingredientsField.getText(), Double.parseDouble(cuisinePriceField.getText()), isDeadly.isSelected(), isVegan.isSelected(), restaurantList.getSelectionModel().getSelectedItem());
        customHibernate.create(cuisine);
    }

    public void updateMenuItem(ActionEvent actionEvent) {
    }

    public void loadRestaurantMenu() {
        cuisineList.getItems().addAll(customHibernate.getRestaurantCuisine(restaurantList.getSelectionModel().getSelectedItem()));
    }
    //</editor-fold>

    //<editor-fold desc="Admin Chat Functionality">
    public void loadChatMessages() {
//        chatMessages.getItems().addAll(customHibernate.getChatMessages(allChats.getSelectionModel().getSelectedItem()));
    }

    public void deleteChat() {
    }

    public void deleteMessage() {
    }

    public void loadChatForm(ActionEvent actionEvent) {

    }
    //</editor-fold>

}
