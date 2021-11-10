package com.geekbrains.network.controllers;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.auth.Login;
import com.geekbrains.model.auth.Registration;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;


@Slf4j
public class AuthController implements Initializable {

    public TextField userName;
    public PasswordField password;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void register(ActionEvent event) {
        if (userName.getText().length() >= 5 && password.getText().length() >= 5) {
            Registration message = new Registration(userName.getText(), password.getText());
            ChatController.net.send(message);
        } else {
            showAlert();
        }
    }

    public void login(ActionEvent event) {
        if (userName.getText().length() >= 3 && password.getText().length() >= 3) {
            Login message = new Login(userName.getText(), password.getText());
            ChatController.net.send(message);

        } else {
            showAlert();
        }
    }


    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("Password or login error");
        alert.setContentText("Password and login could not be less then 5 characters!");
        alert.showAndWait();
    }


}
