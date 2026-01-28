package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainForm implements Initializable {
    //<editor-fold desc="FXML User Tab Elements">
    @FXML public Tab userTab;
    @FXML public TableView<UserTableParameters> userTable;
    @FXML public TableColumn<UserTableParameters, Integer> idCol;
    @FXML public TableColumn<UserTableParameters, String> userTypeCol;
    @FXML public TableColumn<UserTableParameters, String> loginCol;
    @FXML public TableColumn<UserTableParameters, String> passCol;
    @FXML public TableColumn<UserTableParameters, String> nameCol;
    @FXML public TableColumn<UserTableParameters, String> surnameCol;
    @FXML public TableColumn<UserTableParameters, String> phoneCol;
    @FXML public TableColumn<UserTableParameters, String> addrCol;
    @FXML public TableColumn<UserTableParameters, Void> actionCol;

    // User search fields
    @FXML public TextField searchLoginField;
    @FXML public TextField searchNameField;
    @FXML public TextField searchSurnameField;
    @FXML public ComboBox<String> searchRoleField;

    private ObservableList<UserTableParameters> userData = FXCollections.observableArrayList();
    //</editor-fold>

    //<editor-fold desc="FXML Order Tab Elements">
    @FXML public Tab managementTab;
    @FXML public ListView<FoodOrder> ordersList;
    @FXML public TextField titleField;
    @FXML public ComboBox<BasicUser> clientList;
    @FXML public TextField priceField;
    @FXML public ComboBox<Restaurant> restaurantField;
    @FXML public ComboBox<OrderStatus> orderStatusField;
    @FXML public ListView<Cuisine> foodList;
    @FXML public ComboBox<Driver> driverField;

    // Order filters
    @FXML public ComboBox<OrderStatus> filterStatus;
    @FXML public ComboBox<Restaurant> filterRestaurant;
    @FXML public DatePicker filterFrom;
    @FXML public DatePicker filterTo;
    //</editor-fold>

    //<editor-fold desc="FXML Cuisine Tab Elements">
    @FXML public Tab foodTab;
    @FXML public TextField titleCuisineField;
    @FXML public TextArea ingredientsField;
    @FXML public ListView<Restaurant> restaurantList;
    @FXML public TextField cuisinePriceField;
    @FXML public CheckBox isSpicy;
    @FXML public CheckBox isVegan;
    @FXML public ListView<Cuisine> cuisineList;
    //</editor-fold>

    //<editor-fold desc="FXML Chat Tab Elements">
    @FXML public Tab chatTab;
    @FXML public ListView<Chat> allChats;
    @FXML public ListView<Review> chatMessages;
    //</editor-fold>

    //<editor-fold desc="FXML Review Tab Elements">
    @FXML public Tab reviewTab;
    @FXML public ListView<Review> reviewList;
    @FXML public ComboBox<BasicUser> reviewCommentOwner;
    @FXML public ComboBox<BasicUser> reviewFeedbackUser;
    @FXML public TextArea reviewTextField;
    @FXML public Slider ratingSlider;
    @FXML public Label ratingLabel;
    //</editor-fold>

    private EntityManagerFactory entityManagerFactory;
    private CustomHibernate customHibernate;
    private User currentUser;
    private FoodOrder selectedOrder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MainForm initialize() called");
        initializeUserTable();
        initializeOrderStatusField();
        initializeSearchRoleField();
    }

    private void initializeUserTable() {
        if (userTable == null) {
            System.out.println("WARNING: userTable is null in initializeUserTable()");
            return;
        }

        userTable.setEditable(true);

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));

        // Editable password column
        passCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passCol.setOnEditCommit(event -> updateUserField(event, "password"));

        // Editable name column
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> updateUserField(event, "name"));

        // Editable surname column
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        surnameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        surnameCol.setOnEditCommit(event -> updateUserField(event, "surname"));

        // Editable phone column
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));
        phoneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        phoneCol.setOnEditCommit(event -> updateUserField(event, "phone"));

        // Editable address column
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addrCol.setCellFactory(TextFieldTableCell.forTableColumn());
        addrCol.setOnEditCommit(event -> updateUserField(event, "address"));

        // Add delete button column
        addDeleteButtonToTable();
    }

    private void updateUserField(TableColumn.CellEditEvent<UserTableParameters, String> event, String fieldName) {
        UserTableParameters userParam = event.getTableView().getItems().get(event.getTablePosition().getRow());
        String newValue = event.getNewValue();

        User user = customHibernate.getEntityById(User.class, userParam.getId());

        switch (fieldName) {
            case "password":
                user.setPassword(newValue);
                userParam.setPassword(newValue);
                break;
            case "name":
                user.setName(newValue);
                userParam.setName(newValue);
                break;
            case "surname":
                user.setSurname(newValue);
                userParam.setSurname(newValue);
                break;
            case "phone":
                user.setPhoneNumber(newValue);
                userParam.setPhoneNum(newValue);
                break;
            case "address":
                if (user instanceof BasicUser) {
                    ((BasicUser) user).setAddress(newValue);
                    userParam.setAddress(newValue);
                }
                break;
        }

        customHibernate.update(user);
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "User Updated",
                "User information has been updated successfully.");
    }

    private void addDeleteButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    UserTableParameters userParam = getTableView().getItems().get(getIndex());
                    deleteUserFromTable(userParam);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void deleteUserFromTable(UserTableParameters userParam) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + userParam.getLogin() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    customHibernate.delete(User.class, userParam.getId());
                    userData.remove(userParam);
                    FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "User Deleted",
                            "User has been deleted successfully.");
                } catch (Exception e) {
                    FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error deleting user", e);
                }
            }
        });
    }

    private void initializeOrderStatusField() {
        if (orderStatusField != null) {
            orderStatusField.getItems().addAll(OrderStatus.values());
        }
    }

    private void initializeSearchRoleField() {
        if (searchRoleField != null) {
            searchRoleField.getItems().addAll("All", "User", "BasicUser", "Restaurant", "Driver");
            searchRoleField.setValue("All");
        }
    }

    public void setData(EntityManagerFactory entityManagerFactory, User user) {
        System.out.println("setData() called with user: " + user.getLogin());
        this.entityManagerFactory = entityManagerFactory;
        this.currentUser = user;
        this.customHibernate = new CustomHibernate(entityManagerFactory);

        setUserFormVisibility();

        // CRITICAL FIX: Load initial data for the default selected tab
        loadInitialData();
    }

    /**
     * CRITICAL FIX: Load data for the initially selected tab
     */
    private void loadInitialData() {
        System.out.println("loadInitialData() called");

        // Load data for user tab if it's selected and not disabled
        if (userTab != null && userTab.isSelected() && !userTab.isDisabled()) {
            System.out.println("Loading user data on initial load");
            loadUserData();
        }
        // Load data for order management tab if it's the first non-disabled tab
        else if (managementTab != null && managementTab.isSelected() && !managementTab.isDisabled()) {
            System.out.println("Loading order data on initial load");
            loadOrderData();
        }
        // Load data for cuisine tab if it's the first non-disabled tab
        else if (foodTab != null && foodTab.isSelected() && !foodTab.isDisabled()) {
            System.out.println("Loading cuisine data on initial load");
            loadCuisineData();
        }
    }

    private void setUserFormVisibility() {
        if (currentUser instanceof Restaurant) {
            // Restaurant users cannot see user management tab
            if (userTab != null) {
                userTab.setDisable(true);
            }
            if (chatTab != null) {
                chatTab.setDisable(true);
            }
            // Make order management tab the default for restaurants
            if (managementTab != null) {
                managementTab.getTabPane().getSelectionModel().select(managementTab);
            }
        } else if (!(currentUser.isAdmin())) {
            // Non-admin users have limited access
            if (userTab != null) {
                userTab.setDisable(true);
            }
            if (chatTab != null) {
                chatTab.setDisable(true);
            }
            // Make order management tab the default for non-admin users
            if (managementTab != null) {
                managementTab.getTabPane().getSelectionModel().select(managementTab);
            }
        }
    }

    //<editor-fold desc="User Tab Functionality">
    public void reloadTableData() {
        System.out.println("reloadTableData() called");

        if (userTab != null && userTab.isSelected()) {
            System.out.println("Reloading user tab data");
            loadUserData();
        } else if (managementTab != null && managementTab.isSelected()) {
            System.out.println("Reloading order tab data");
            loadOrderData();
        } else if (foodTab != null && foodTab.isSelected()) {
            System.out.println("Reloading cuisine tab data");
            loadCuisineData();
        } else if (chatTab != null && chatTab.isSelected()) {
            System.out.println("Reloading chat tab data");
            loadChatData();
        } else if (reviewTab != null && reviewTab.isSelected()) {
            System.out.println("Reloading review tab data");
            loadReviewData();
        }
    }

    private void loadUserData() {
        try {
            System.out.println("loadUserData() started");
            userData.clear();
            List<User> users = customHibernate.getAllRecords(User.class);
            System.out.println("Found " + users.size() + " users in database");

            for (User u : users) {
                UserTableParameters userParam = new UserTableParameters();
                userParam.setId(u.getId());
                userParam.setUserType(u.getClass().getSimpleName());
                userParam.setLogin(u.getLogin());
                userParam.setPassword(u.getPassword());
                userParam.setName(u.getName());
                userParam.setSurname(u.getSurname());
                userParam.setPhoneNum(u.getPhoneNumber());

                if (u instanceof BasicUser) {
                    userParam.setAddress(((BasicUser) u).getAddress());
                }

                userData.add(userParam);
                System.out.println("Added user: " + u.getLogin());
            }

            userTable.setItems(userData);
            System.out.println("User table items set. Total: " + userData.size());
        } catch (Exception e) {
            System.err.println("Error loading user data: " + e.getMessage());
            e.printStackTrace();
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error loading user data", e);
        }
    }

    public void searchUsers() {
        String login = searchLoginField.getText().toLowerCase();
        String name = searchNameField.getText().toLowerCase();
        String surname = searchSurnameField.getText().toLowerCase();
        String role = searchRoleField.getValue();

        ObservableList<UserTableParameters> filteredData = userData.filtered(user -> {
            boolean matchLogin = login.isEmpty() || user.getLogin().toLowerCase().contains(login);
            boolean matchName = name.isEmpty() || user.getName().toLowerCase().contains(name);
            boolean matchSurname = surname.isEmpty() || user.getSurname().toLowerCase().contains(surname);
            boolean matchRole = role.equals("All") || user.getUserType().equals(role);

            return matchLogin && matchName && matchSurname && matchRole;
        });

        userTable.setItems(filteredData);
    }

    public void clearUserSearch() {
        searchLoginField.clear();
        searchNameField.clear();
        searchSurnameField.clear();
        searchRoleField.setValue("All");
        userTable.setItems(userData);
    }

    public void addNewUser() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, null, false);

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setTitle("Add New User");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        // Reload data after closing
        loadUserData();
    }
    //</editor-fold>

    //<editor-fold desc="Order Tab Functionality">
    private void loadOrderData() {
        try {
            System.out.println("loadOrderData() started");
            clearAllOrderFields();

            List<FoodOrder> foodOrders = getFoodOrders();
            System.out.println("Found " + foodOrders.size() + " orders");
            ordersList.getItems().addAll(foodOrders);

            List<BasicUser> clients = customHibernate.getAllRecords(BasicUser.class);
            System.out.println("Found " + clients.size() + " clients");
            clientList.getItems().addAll(clients);

            List<Restaurant> restaurants = customHibernate.getAllRecords(Restaurant.class);
            System.out.println("Found " + restaurants.size() + " restaurants");
            restaurantField.getItems().addAll(restaurants);

            List<Driver> drivers = customHibernate.getAllRecords(Driver.class);
            System.out.println("Found " + drivers.size() + " drivers");
            driverField.getItems().addAll(drivers);

            orderStatusField.getItems().clear();
            orderStatusField.getItems().addAll(OrderStatus.values());

            // Load filter options
            filterStatus.getItems().add(null); // For "All"
            filterStatus.getItems().addAll(OrderStatus.values());
            filterRestaurant.getItems().add(null); // For "All"
            filterRestaurant.getItems().addAll(restaurants);

            System.out.println("Order data loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading order data: " + e.getMessage());
            e.printStackTrace();
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error loading order data", e);
        }
    }

    private List<FoodOrder> getFoodOrders() {
        if (currentUser instanceof Restaurant) {
            return customHibernate.getRestaurantOrders((Restaurant) currentUser);
        } else {
            return customHibernate.getAllRecords(FoodOrder.class);
        }
    }

    private void clearAllOrderFields() {
        ordersList.getItems().clear();
        clientList.getItems().clear();
        restaurantField.getItems().clear();
        driverField.getItems().clear();
        foodList.getItems().clear();
        titleField.clear();
        priceField.clear();
        filterStatus.getItems().clear();
        filterRestaurant.getItems().clear();
    }

    public void createOrder() {
        try {
            if (!validateOrderInput()) {
                return;
            }

            List<Cuisine> selectedCuisines = foodList.getSelectionModel().getSelectedItems();

            // Calculate base price
            double basePrice = selectedCuisines.stream()
                    .mapToDouble(Cuisine::getPrice)
                    .sum();

            // Apply dynamic pricing if needed
            double finalPrice = customHibernate.calculateDynamicPrice(basePrice);

            FoodOrder foodOrder = new FoodOrder(
                    titleField.getText(),
                    finalPrice,
                    clientList.getValue(),
                    selectedCuisines,
                    restaurantField.getValue()
            );

            foodOrder.setOrderStatus(OrderStatus.PLACED);
            foodOrder.setDateCreated(LocalDate.now());

            customHibernate.create(foodOrder);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Order Created",
                    "Order has been created successfully with price: €" + String.format("%.2f", finalPrice));

            fillOrderLists();
            clearOrderInputFields();

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error creating order", e);
        }
    }

    private boolean validateOrderInput() {
        if (titleField.getText().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter order title.");
            return false;
        }

        if (clientList.getValue() == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please select a customer.");
            return false;
        }

        if (restaurantField.getValue() == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please select a restaurant.");
            return false;
        }

        if (foodList.getSelectionModel().getSelectedItems().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please select at least one dish.");
            return false;
        }

        return true;
    }

    public void updateOrder() {
        try {
            if (selectedOrder == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Order Selected",
                        "Please select an order to update.");
                return;
            }

            // Check if order can be modified
            if (selectedOrder.getOrderStatus() == OrderStatus.COMPLETED) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Order Completed",
                        "Cannot modify completed orders.");
                return;
            }

            // Update order details
            selectedOrder.setRestaurant(restaurantField.getValue());
            selectedOrder.setName(titleField.getText());
            selectedOrder.setOrderStatus(orderStatusField.getValue());
            selectedOrder.setBuyer(clientList.getValue());
            selectedOrder.setDateUpdated(LocalDate.now());

            // Recalculate price if cuisines changed
            List<Cuisine> selectedCuisines = foodList.getSelectionModel().getSelectedItems();
            if (!selectedCuisines.isEmpty()) {
                double basePrice = selectedCuisines.stream()
                        .mapToDouble(Cuisine::getPrice)
                        .sum();
                selectedOrder.setPrice(customHibernate.calculateDynamicPrice(basePrice));
                selectedOrder.setCuisineList(selectedCuisines);
            }

            // Assign driver if selected
            if (driverField.getValue() != null && selectedOrder.getOrderStatus() != OrderStatus.PLACED) {
                selectedOrder.setDriver(driverField.getValue());
            }

            customHibernate.update(selectedOrder);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Order Updated",
                    "Order has been updated successfully.");

            fillOrderLists();

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error updating order", e);
        }
    }

    public void deleteOrder() {
        try {
            FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Order Selected",
                        "Please select an order to delete.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Order");
            confirmAlert.setContentText("Are you sure you want to delete this order?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    customHibernate.delete(FoodOrder.class, selectedOrder.getId());
                    fillOrderLists();
                    clearOrderInputFields();
                    FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Order Deleted",
                            "Order has been deleted successfully.");
                }
            });

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error deleting order", e);
        }
    }

    public void loadOrderInfo() {
        selectedOrder = ordersList.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            return;
        }

        // Populate fields
        titleField.setText(selectedOrder.getName());
        priceField.setText(selectedOrder.getPrice().toString());

        // Select customer
        clientList.getItems().stream()
                .filter(c -> c.getId() == selectedOrder.getBuyer().getId())
                .findFirst()
                .ifPresent(u -> clientList.getSelectionModel().select(u));

        // Select restaurant
        restaurantField.getItems().stream()
                .filter(r -> r.getId() == selectedOrder.getRestaurant().getId())
                .findFirst()
                .ifPresent(r -> restaurantField.getSelectionModel().select(r));

        // Select order status
        orderStatusField.setValue(selectedOrder.getOrderStatus());

        // Select driver if assigned
        if (selectedOrder.getDriver() != null) {
            driverField.getItems().stream()
                    .filter(d -> d.getId() == selectedOrder.getDriver().getId())
                    .findFirst()
                    .ifPresent(d -> driverField.getSelectionModel().select(d));
        }

        // Load restaurant menu
        loadRestaurantMenuForOrder();

        // Disable fields if order is completed
        disableFoodOrderFields();
    }

    private void disableFoodOrderFields() {
        boolean isCompleted = selectedOrder != null &&
                selectedOrder.getOrderStatus() == OrderStatus.COMPLETED;

        clientList.setDisable(isCompleted);
        priceField.setDisable(isCompleted);
        restaurantField.setDisable(isCompleted);
        foodList.setDisable(isCompleted);
        driverField.setDisable(isCompleted);
    }

    private void clearOrderInputFields() {
        titleField.clear();
        priceField.clear();
        clientList.getSelectionModel().clearSelection();
        restaurantField.getSelectionModel().clearSelection();
        orderStatusField.getSelectionModel().clearSelection();
        driverField.getSelectionModel().clearSelection();
        foodList.getItems().clear();
        selectedOrder = null;
    }

    private void fillOrderLists() {
        ordersList.getItems().clear();
        ordersList.getItems().addAll(getFoodOrders());
    }

    public void filterOrders() {
        OrderStatus status = filterStatus.getValue();
        Restaurant restaurant = filterRestaurant.getValue();
        LocalDate fromDate = filterFrom.getValue();
        LocalDate toDate = filterTo.getValue();

        List<FoodOrder> allOrders = getFoodOrders();

        List<FoodOrder> filteredOrders = allOrders.stream()
                .filter(order -> status == null || order.getOrderStatus() == status)
                .filter(order -> restaurant == null || order.getRestaurant().getId() == restaurant.getId())
                .filter(order -> fromDate == null || order.getDateCreated() == null ||
                        !order.getDateCreated().isBefore(fromDate))
                .filter(order -> toDate == null || order.getDateCreated() == null ||
                        !order.getDateCreated().isAfter(toDate))
                .collect(Collectors.toList());

        ordersList.getItems().clear();
        ordersList.getItems().addAll(filteredOrders);
    }

    public void clearOrderFilters() {
        filterStatus.setValue(null);
        filterRestaurant.setValue(null);
        filterFrom.setValue(null);
        filterTo.setValue(null);
        fillOrderLists();
    }

    public void loadRestaurantMenuForOrder() {
        Restaurant selectedRestaurant = restaurantField.getValue();

        if (selectedRestaurant != null) {
            foodList.getItems().clear();
            List<Cuisine> menu = customHibernate.getRestaurantCuisine(selectedRestaurant);
            foodList.getItems().addAll(menu);

            // Pre-select items if editing existing order
            if (selectedOrder != null && selectedOrder.getCuisineList() != null) {
                selectedOrder.getCuisineList().forEach(cuisine -> {
                    foodList.getSelectionModel().select(cuisine);
                });
            }
        }
    }

    public void loadChatForm() throws IOException {
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Order Selected",
                    "Please select an order to view chat.");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-form.fxml"));
        Parent parent = fxmlLoader.load();

        ChatForm chatForm = fxmlLoader.getController();
        chatForm.setData(entityManagerFactory, currentUser, selectedOrder);

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setTitle("Order Chat - " + selectedOrder.getName());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
    //</editor-fold>

    //<editor-fold desc="Cuisine Tab Functionality">
    private void loadCuisineData() {
        try {
            System.out.println("loadCuisineData() started");
            clearAllCuisineFields();
            List<Restaurant> restaurants = customHibernate.getAllRecords(Restaurant.class);
            System.out.println("Found " + restaurants.size() + " restaurants for cuisine");
            restaurantList.getItems().addAll(restaurants);
        } catch (Exception e) {
            System.err.println("Error loading cuisine data: " + e.getMessage());
            e.printStackTrace();
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error loading cuisine data", e);
        }
    }

    private void clearAllCuisineFields() {
        cuisineList.getItems().clear();
        cuisinePriceField.clear();
        titleCuisineField.clear();
        ingredientsField.clear();
        isSpicy.setSelected(false);
        isVegan.setSelected(false);
        restaurantList.getItems().clear();
    }

    public void createNewMenuItem() {
        try {
            if (!validateCuisineInput()) {
                return;
            }

            Cuisine cuisine = new Cuisine(
                    titleCuisineField.getText(),
                    ingredientsField.getText(),
                    Double.parseDouble(cuisinePriceField.getText()),
                    isSpicy.isSelected(),
                    isVegan.isSelected(),
                    restaurantList.getSelectionModel().getSelectedItem()
            );

            customHibernate.create(cuisine);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Created",
                    "Menu item has been created successfully.");

            loadRestaurantMenu();
            clearCuisineInputFields();

        } catch (NumberFormatException e) {
            FxUtils.generateAlert(Alert.AlertType.ERROR, "Invalid Input", "Price Error",
                    "Please enter a valid price.");
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error creating menu item", e);
        }
    }

    private boolean validateCuisineInput() {
        if (titleCuisineField.getText().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter dish name.");
            return false;
        }

        if (cuisinePriceField.getText().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter price.");
            return false;
        }

        if (restaurantList.getSelectionModel().getSelectedItem() == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please select a restaurant.");
            return false;
        }

        return true;
    }

    private void clearCuisineInputFields() {
        titleCuisineField.clear();
        ingredientsField.clear();
        cuisinePriceField.clear();
        isSpicy.setSelected(false);
        isVegan.setSelected(false);
    }

    public void updateMenuItem() {
        try {
            Cuisine selectedCuisine = cuisineList.getSelectionModel().getSelectedItem();

            if (selectedCuisine == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Item Selected",
                        "Please select a menu item to update.");
                return;
            }

            selectedCuisine.setName(titleCuisineField.getText());
            selectedCuisine.setIngredients(ingredientsField.getText());
            selectedCuisine.setPrice(Double.parseDouble(cuisinePriceField.getText()));
            selectedCuisine.setSpicy(isSpicy.isSelected());
            selectedCuisine.setVegan(isVegan.isSelected());

            customHibernate.update(selectedCuisine);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Updated",
                    "Menu item has been updated successfully.");

            loadRestaurantMenu();

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error updating menu item", e);
        }
    }

    public void deleteMenuItem() {
        try {
            Cuisine selectedCuisine = cuisineList.getSelectionModel().getSelectedItem();

            if (selectedCuisine == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Item Selected",
                        "Please select a menu item to delete.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Menu Item");
            confirmAlert.setContentText("Are you sure you want to delete this menu item?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    customHibernate.delete(Cuisine.class, selectedCuisine.getId());
                    loadRestaurantMenu();
                    clearCuisineInputFields();
                    FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Deleted",
                            "Menu item has been deleted successfully.");
                }
            });

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error deleting menu item", e);
        }
    }

    public void loadRestaurantMenu() {
        Restaurant selectedRestaurant = restaurantList.getSelectionModel().getSelectedItem();

        if (selectedRestaurant != null) {
            cuisineList.getItems().clear();
            cuisineList.getItems().addAll(
                    customHibernate.getRestaurantCuisine(selectedRestaurant)
            );
        }
    }

    public void loadCuisineInfo() {
        Cuisine selectedCuisine = cuisineList.getSelectionModel().getSelectedItem();

        if (selectedCuisine != null) {
            titleCuisineField.setText(selectedCuisine.getName());
            ingredientsField.setText(selectedCuisine.getIngredients());
            cuisinePriceField.setText(selectedCuisine.getPrice().toString());
            isSpicy.setSelected(selectedCuisine.isSpicy());
            isVegan.setSelected(selectedCuisine.isVegan());
        }
    }
    //</editor-fold>

    //<editor-fold desc="Chat Tab Functionality">
    private void loadChatData() {
        try {
            System.out.println("loadChatData() started");
            allChats.getItems().clear();
            List<Chat> chats = customHibernate.getAllRecords(Chat.class);
            System.out.println("Found " + chats.size() + " chats");
            allChats.getItems().addAll(chats);
        } catch (Exception e) {
            System.err.println("Error loading chat data: " + e.getMessage());
            e.printStackTrace();
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error loading chat data", e);
        }
    }

    public void loadChatMessages() {
        Chat selectedChat = allChats.getSelectionModel().getSelectedItem();

        if (selectedChat != null) {
            chatMessages.getItems().clear();
            Chat chat = customHibernate.getEntityById(Chat.class, selectedChat.getId());
            if (chat.getMessages() != null) {
                chatMessages.getItems().addAll(chat.getMessages());
            }
        }
    }

    public void deleteChat() {
        try {
            Chat selectedChat = allChats.getSelectionModel().getSelectedItem();

            if (selectedChat == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Chat Selected",
                        "Please select a chat to delete.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Chat");
            confirmAlert.setContentText("Are you sure you want to delete this chat?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    customHibernate.delete(Chat.class, selectedChat.getId());
                    loadChatData();
                    chatMessages.getItems().clear();
                    FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Chat Deleted",
                            "Chat has been deleted successfully.");
                }
            });

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error deleting chat", e);
        }
    }

    public void deleteMessage() {
        try {
            Review selectedMessage = chatMessages.getSelectionModel().getSelectedItem();

            if (selectedMessage == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Message Selected",
                        "Please select a message to delete.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Message");
            confirmAlert.setContentText("Are you sure you want to delete this message?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    customHibernate.delete(Review.class, selectedMessage.getId());
                    loadChatMessages();
                    FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Message Deleted",
                            "Message has been deleted successfully.");
                }
            });

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error deleting message", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Review Tab Functionality">
    private void loadReviewData() {
        try {
            System.out.println("loadReviewData() started");
            reviewList.getItems().clear();
            List<Review> reviews = customHibernate.getAllRecords(Review.class);
            System.out.println("Found " + reviews.size() + " reviews");
            reviewList.getItems().addAll(reviews);

            reviewCommentOwner.getItems().clear();
            List<BasicUser> users = customHibernate.getAllRecords(BasicUser.class);
            reviewCommentOwner.getItems().addAll(users);

            reviewFeedbackUser.getItems().clear();
            reviewFeedbackUser.getItems().addAll(users);

            // Initialize rating slider
            if (ratingSlider != null) {
                ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    ratingLabel.setText(String.format("Rating: %d", newVal.intValue()));
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading review data: " + e.getMessage());
            e.printStackTrace();
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error loading review data", e);
        }
    }

    public void createReview() {
        try {
            if (reviewCommentOwner.getValue() == null || reviewFeedbackUser.getValue() == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                        "Please select both comment owner and feedback user.");
                return;
            }

            Review review = new Review();
            review.setCommentOwner(reviewCommentOwner.getValue());
            review.setFeedbackUser(reviewFeedbackUser.getValue());
            review.setReviewText(reviewTextField.getText());
            review.setRating((int) ratingSlider.getValue());
            review.setDateCreated(LocalDate.now());

            customHibernate.create(review);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Review Created",
                    "Review has been created successfully.");

            loadReviewData();
            clearReviewInputFields();

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error creating review", e);
        }
    }

    private void clearReviewInputFields() {
        reviewCommentOwner.getSelectionModel().clearSelection();
        reviewFeedbackUser.getSelectionModel().clearSelection();
        reviewTextField.clear();
        ratingSlider.setValue(5);
    }

    public void deleteReview() {
        try {
            Review selectedReview = reviewList.getSelectionModel().getSelectedItem();

            if (selectedReview == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "No Review Selected",
                        "Please select a review to delete.");
                return;
            }

            customHibernate.delete(Review.class, selectedReview.getId());
            loadReviewData();

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Review Deleted",
                    "Review has been deleted successfully.");

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error deleting review", e);
        }
    }
    //</editor-fold>
}