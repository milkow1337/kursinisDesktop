package com.example.courseprifs.fxControllers;

import com.example.courseprifs.hibernateControl.GenericHibernate;
import com.example.courseprifs.model.Restaurant;
import com.example.courseprifs.model.User;
import com.example.courseprifs.model.VehicleType;
import jakarta.persistence.EntityManagerFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class UserForm implements Initializable {

    @FXML
    public RadioButton userRadio;
    @FXML
    public RadioButton restaurantRadio;
    @FXML
    public RadioButton clientRadio;
    @FXML
    public RadioButton driverRadio;
    @FXML
    public ToggleGroup Select;
    @FXML
    public TextField addressField;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nameField;
    @FXML
    public TextField surnameField;
    @FXML
    public TextField phoneField;
    public Button updateButton;
    public ComboBox<VehicleType> comboTest;

    private EntityManagerFactory entityManagerFactory;
    private GenericHibernate genericHibernate;
    private User userForUpdate;
    private boolean isForUpdate;

    public void setData(EntityManagerFactory entityManagerFactory, User user, boolean isForUpdate) {
        this.entityManagerFactory = entityManagerFactory;
        this.genericHibernate = new GenericHibernate(entityManagerFactory);
        this.userForUpdate = user;
        this.isForUpdate = isForUpdate;
        fillUserDataForUpdate();
    }

    private void fillUserDataForUpdate() {
        if(userForUpdate != null && isForUpdate){
            if(userForUpdate instanceof User){
                usernameField.setText(userForUpdate.getLogin());
                passwordField.setText(userForUpdate.getPassword());
                //likusius reiktu pabaigti
            }
        }else{
            updateButton.setVisible(false);
        }
    }

    public void disableFields() {
        if (userRadio.isSelected()) {
            addressField.setDisable(true);
        } else if (restaurantRadio.isSelected()) {
            restaurantRadio.setSelected(true);
            addressField.setDisable(false);
        } else if (clientRadio.isSelected()) {
            clientRadio.setSelected(true);
            addressField.setDisable(false);
        } else if (driverRadio.isSelected()) {
            driverRadio.setSelected(true);
            addressField.setDisable(false);
        } else {
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        disableFields();
        comboTest.getItems().addAll(VehicleType.values());
    }

    public void createNewUser() {
        if (userRadio.isSelected()) {
            User user = new User(usernameField.getText(), passwordField.getText(), nameField.getText(), surnameField.getText(), phoneField.getText());
            genericHibernate.create(user);
        } else if (restaurantRadio.isSelected()) {
            Restaurant restaurant = new Restaurant(usernameField.getText(), passwordField.getText(), nameField.getText(), surnameField.getText(), phoneField.getText(), addressField.getText());
            genericHibernate.create(restaurant);
        }
    }

    public void updateUser(ActionEvent actionEvent) {
    }
}
