package com.geekbrains.network.controllers;


import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.auth.Login;
import com.geekbrains.model.auth.Registration;
import com.geekbrains.network.Net;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;


@Slf4j
public class AuthController implements Initializable {

    public TextField userName;
    public PasswordField password;
    public String UserId;
    public Button register;
    public Button login;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void auth(ActionEvent event) {
        Button sourceBtn = (Button) event.getSource();
        AbstractMessage message = null;
        if (checkSymbols()) {
            if (sourceBtn.equals(register)) {
                message = new Registration(userName.getText(), password.getText(),null);
            } else if (sourceBtn.equals(login)) {
                message = new Login(userName.getText(), password.getText(),null);
            }
            ChatController.net.send(message);
        } else {
            showAlert();
        }
    }

    private boolean checkSymbols() {
        return userName.getText().length() >= 5 && password.getText().length() >= 5;
    }


    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("Password or login error");
        alert.setContentText("Password and login could not be less then 5 characters!");
        alert.showAndWait();
    }


}
