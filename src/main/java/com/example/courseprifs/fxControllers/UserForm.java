package com.example.courseprifs.fxControllers;

import com.example.courseprifs.hibernateControl.GenericHibernate;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class UserForm implements Initializable {

    @FXML public RadioButton userRadio;
    @FXML public RadioButton restaurantRadio;
    @FXML public RadioButton clientRadio;
    @FXML public RadioButton driverRadio;
    @FXML public ToggleGroup userTypeGroup;

    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public TextField nameField;
    @FXML public TextField surnameField;
    @FXML public TextField phoneField;
    @FXML public TextField addressField;

    // Restaurant-specific fields
    @FXML public Label restaurantNameLabel;
    @FXML public TextField restaurantNameField;
    @FXML public Label openingTimeLabel;
    @FXML public TextField openingTimeField;
    @FXML public Label closingTimeLabel;
    @FXML public TextField closingTimeField;

    // Driver-specific fields
    @FXML public Label licenceLabel;
    @FXML public TextField licenceField;
    @FXML public Label birthdateLabel;
    @FXML public DatePicker birthdatePicker;
    @FXML public Label vehicleLabel;
    @FXML public ComboBox<VehicleType> vehicleTypeCombo;

    @FXML public Button saveButton;
    @FXML public Button updateButton;

    private EntityManagerFactory entityManagerFactory;
    private GenericHibernate genericHibernate;
    private User userForUpdate;
    private boolean isForUpdate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup vehicle type combo box
        if (vehicleTypeCombo != null) {
            vehicleTypeCombo.getItems().addAll(VehicleType.values());
        }

        // Add listeners to radio buttons for dynamic field visibility
        setupRadioButtonListeners();
    }

    private void setupRadioButtonListeners() {
        if (userRadio != null) {
            userRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    toggleDriverFields(false);
                    toggleRestaurantFields(false);
                    addressField.setDisable(true);
                }
            });
        }

        if (restaurantRadio != null) {
            restaurantRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    toggleDriverFields(false);
                    toggleRestaurantFields(true);
                    addressField.setDisable(false);
                }
            });
        }

        if (clientRadio != null) {
            clientRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    toggleDriverFields(false);
                    toggleRestaurantFields(false);
                    addressField.setDisable(false);
                }
            });
        }

        if (driverRadio != null) {
            driverRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    toggleDriverFields(true);
                    toggleRestaurantFields(false);
                    addressField.setDisable(false);
                }
            });
        }
    }

    private void toggleDriverFields(boolean show) {
        // Toggle visibility and managed property for driver-specific fields
        if (licenceLabel != null) {
            licenceLabel.setVisible(show);
            licenceLabel.setManaged(show);
        }
        if (licenceField != null) {
            licenceField.setVisible(show);
            licenceField.setManaged(show);
        }
        if (birthdateLabel != null) {
            birthdateLabel.setVisible(show);
            birthdateLabel.setManaged(show);
        }
        if (birthdatePicker != null) {
            birthdatePicker.setVisible(show);
            birthdatePicker.setManaged(show);
        }
        if (vehicleLabel != null) {
            vehicleLabel.setVisible(show);
            vehicleLabel.setManaged(show);
        }
        if (vehicleTypeCombo != null) {
            vehicleTypeCombo.setVisible(show);
            vehicleTypeCombo.setManaged(show);
        }
    }

    private void toggleRestaurantFields(boolean show) {
        if (restaurantNameLabel != null) {
            restaurantNameLabel.setVisible(show);
            restaurantNameLabel.setManaged(show);
        }
        if (restaurantNameField != null) {
            restaurantNameField.setVisible(show);
            restaurantNameField.setManaged(show);
        }
        if (openingTimeLabel != null) {
            openingTimeLabel.setVisible(show);
            openingTimeLabel.setManaged(show);
        }
        if (openingTimeField != null) {
            openingTimeField.setVisible(show);
            openingTimeField.setManaged(show);
        }
        if (closingTimeLabel != null) {
            closingTimeLabel.setVisible(show);
            closingTimeLabel.setManaged(show);
        }
        if (closingTimeField != null) {
            closingTimeField.setVisible(show);
            closingTimeField.setManaged(show);
        }
    }

    public void setData(EntityManagerFactory entityManagerFactory, User user, boolean isForUpdate) {
        this.entityManagerFactory = entityManagerFactory;
        this.genericHibernate = new GenericHibernate(entityManagerFactory);
        this.userForUpdate = user;
        this.isForUpdate = isForUpdate;

        fillUserDataForUpdate();
        configureButtonVisibility();
    }

    private void fillUserDataForUpdate() {
        if (userForUpdate != null && isForUpdate) {
            // Fill common fields
            usernameField.setText(userForUpdate.getLogin());
            usernameField.setDisable(true); // Cannot change username
            passwordField.setText(userForUpdate.getPassword());
            nameField.setText(userForUpdate.getName());
            surnameField.setText(userForUpdate.getSurname());
            phoneField.setText(userForUpdate.getPhoneNumber());
            // isAdminCheck.setSelected(userForUpdate.isAdmin()); // Removed isAdminCheck

            // Select appropriate radio button and fill type-specific fields
            if (userForUpdate instanceof Driver) {
                driverRadio.setSelected(true);
                Driver driver = (Driver) userForUpdate;
                addressField.setText(driver.getAddress());
                licenceField.setText(driver.getLicence());
                birthdatePicker.setValue(driver.getBDate());
                vehicleTypeCombo.setValue(driver.getVehicleType());
                toggleDriverFields(true);
                toggleRestaurantFields(false);
            } else if (userForUpdate instanceof Restaurant) {
                restaurantRadio.setSelected(true);
                Restaurant restaurant = (Restaurant) userForUpdate;
                addressField.setText(restaurant.getAddress());
                restaurantNameField.setText(restaurant.getRestaurantName());
                if (restaurant.getOpeningTime() != null) openingTimeField.setText(restaurant.getOpeningTime().toString());
                if (restaurant.getClosingTime() != null) closingTimeField.setText(restaurant.getClosingTime().toString());
                toggleDriverFields(false);
                toggleRestaurantFields(true);
            } else if (userForUpdate instanceof BasicUser) {
                clientRadio.setSelected(true);
                BasicUser basicUser = (BasicUser) userForUpdate;
                addressField.setText(basicUser.getAddress());
                toggleDriverFields(false);
                toggleRestaurantFields(false);
            } else { // Base User
                userRadio.setSelected(true);
                toggleDriverFields(false);
                toggleRestaurantFields(false);
            }

            // Disable radio buttons when updating
            userRadio.setDisable(true);
            restaurantRadio.setDisable(true);
            clientRadio.setDisable(true);
            driverRadio.setDisable(true);
        }
    }

    private void configureButtonVisibility() {
        if (isForUpdate) {
            saveButton.setVisible(false);
            updateButton.setVisible(true);
        } else {
            saveButton.setVisible(true);
            updateButton.setVisible(false);
        }
    }

    @FXML
    public void createNewUser() {
        if (!validateInput()) {
            return;
        }

        try {
            User newUser = buildUserFromInput();

            if (newUser == null) {
                FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "User Creation Failed",
                        "Please select a user type.");
                return;
            }

            genericHibernate.create(newUser);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "User Created",
                    "User has been created successfully. You can now log in.");

            closeWindow();

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error creating user", e);
        }
    }

    @FXML
    public void updateUser() {
        if (!validateInput()) {
            return;
        }

        try {
            // Update common fields
            userForUpdate.setPassword(passwordField.getText());
            userForUpdate.setName(nameField.getText());
            userForUpdate.setSurname(surnameField.getText());
            userForUpdate.setPhoneNumber(phoneField.getText());
            // userForUpdate.setAdmin(isAdminCheck.isSelected()); // Removed isAdminCheck

            // Update type-specific fields
            if (userForUpdate instanceof BasicUser) {
                ((BasicUser) userForUpdate).setAddress(addressField.getText());
            }

            if (userForUpdate instanceof Driver) {
                Driver driver = (Driver) userForUpdate;
                driver.setLicence(licenceField.getText());
                driver.setBDate(birthdatePicker.getValue());
                driver.setVehicleType(vehicleTypeCombo.getValue());
            }

            if (userForUpdate instanceof Restaurant) {
                Restaurant restaurant = (Restaurant) userForUpdate;
                restaurant.setRestaurantName(restaurantNameField.getText());
                restaurant.setOpeningTime(LocalTime.parse(openingTimeField.getText()));
                restaurant.setClosingTime(LocalTime.parse(closingTimeField.getText()));
            }

            genericHibernate.update(userForUpdate);

            FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "User Updated",
                    "User has been updated successfully.");

            closeWindow();

        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Error updating user", e);
        }
    }

    private User buildUserFromInput() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        // boolean isAdmin = isAdminCheck.isSelected(); // Removed isAdminCheck

        User user = null;

        if (userRadio.isSelected()) {
            user = new User(username, password, name, surname, phone);
            // Force admin for User type
            user.setAdmin(true);
        } else if (restaurantRadio.isSelected()) {
            String restaurantName = restaurantNameField.getText().trim();
            LocalTime openingTime = LocalTime.parse(openingTimeField.getText().trim());
            LocalTime closingTime = LocalTime.parse(closingTimeField.getText().trim());
            
            user = new Restaurant(username, password, name, surname, phone, address, restaurantName, openingTime, closingTime);
            user.setAdmin(false); // Restaurants are not admins by default
        } else if (clientRadio.isSelected()) {
            user = new BasicUser(username, password, name, surname, phone, address);
            user.setAdmin(false); // BasicUsers are not admins by default
        } else if (driverRadio.isSelected()) {
            String licence = licenceField.getText().trim();
            LocalDate birthdate = birthdatePicker.getValue();
            VehicleType vehicleType = vehicleTypeCombo.getValue();

            if (licence.isEmpty() || birthdate == null || vehicleType == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Driver Information",
                        "Please fill in all driver-specific fields (License, Birth Date, and Vehicle Type).");
                return null;
            }

            user = new Driver(username, password, name, surname, phone, address,
                    licence, birthdate, vehicleType);
            user.setAdmin(false); // Drivers are not admins by default
        }

        return user;
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter a username.");
            return false;
        }

        if (passwordField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter a password.");
            return false;
        }

        if (nameField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter a first name.");
            return false;
        }

        if (surnameField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter a last name.");
            return false;
        }

        if (phoneField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter a phone number.");
            return false;
        }

        // Validate type-specific fields
        if ((restaurantRadio.isSelected() || clientRadio.isSelected() || driverRadio.isSelected())
                && addressField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                    "Please enter an address.");
            return false;
        }

        // Additional validation for driver fields
        if (driverRadio.isSelected()) {
            if (licenceField.getText().trim().isEmpty()) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                        "Please enter a license number.");
                return false;
            }

            if (birthdatePicker.getValue() == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                        "Please select a birth date.");
                return false;
            }

            if (vehicleTypeCombo.getValue() == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                        "Please select a vehicle type.");
                return false;
            }

            // Validate age (must be at least 18 years old)
            LocalDate minDate = LocalDate.now().minusYears(18);
            if (birthdatePicker.getValue().isAfter(minDate)) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Age Requirement",
                        "Driver must be at least 18 years old.");
                return false;
            }
        }

        // Additional validation for restaurant fields
        if (restaurantRadio.isSelected()) {
            if (restaurantNameField.getText().trim().isEmpty()) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Information",
                        "Please enter a restaurant name.");
                return false;
            }
            
            try {
                LocalTime.parse(openingTimeField.getText().trim());
            } catch (DateTimeParseException e) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Time Format",
                        "Please enter opening time in HH:mm format (e.g., 09:00).");
                return false;
            }
            
            try {
                LocalTime.parse(closingTimeField.getText().trim());
            } catch (DateTimeParseException e) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Time Format",
                        "Please enter closing time in HH:mm format (e.g., 22:00).");
                return false;
            }
        }

        return true;
    }

    @FXML
    public void closeWindow() {
        if (usernameField != null && usernameField.getScene() != null) {
            usernameField.getScene().getWindow().hide();
        }
    }
}
