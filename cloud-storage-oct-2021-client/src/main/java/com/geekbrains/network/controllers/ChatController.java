package com.geekbrains.network.controllers;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.auth.Login;
import com.geekbrains.model.auth.Registration;
import com.geekbrains.model.navigation.FileMessage;
import com.geekbrains.model.navigation.FileRequest;
import com.geekbrains.model.navigation.ListMessage;
import com.geekbrains.network.App;
import com.geekbrains.network.Net;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import javafx.scene.control.MenuItem;

import java.io.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import java.util.ResourceBundle;


import static javafx.application.Platform.runLater;

@Slf4j
@Getter
public class ChatController implements Initializable {

    @FXML
    private Path clientDir;
    private final String ROOT_DIR = "clients";

    public ListView<String> listViewClient;
    public TextArea absolutPathClient;
    public TextArea fileNameClient;

    public ListView<String> listViewServer;
    public TextArea absolutPathServer;
    public TextArea fileNameServer;

    public Button upload;
    public Button download;

    public static Net net;
    private byte[] buffer;
    public MenuItem reg;
    private String userId = null;


    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buffer = new byte[8192];

        net = Net.getInstance(this::processMessage);
        addViewListener(listViewClient, fileNameClient);
        addViewListener(listViewServer, fileNameServer);


    }


    private void processMessage(AbstractMessage message) throws IOException {
        log.debug("Start processing {}", message);
        switch (message.getType()) {
            case FILE_MESSAGE:
                fileMessage((FileMessage) message);
                break;
            case LIST_MESSAGE:
                listMessage((ListMessage) message);
                break;
            case LOGIN:
                login((Login) message);
                break;
            case REGISTRATION:
                registration((Registration) message);
                break;
        }
    }

    private void registration(Registration message) {
        runLater(()->{
            userId = message.getUserId();
            createClientDir(message.getUsername());
            App.auth.close();
        });
    }


    private void login(Login message) {
        runLater(() -> {
            if (message.getUserId() == null) {
                showAlert();
            } else {
                userId = message.getUserId();
                createClientDir(message.getUsername());
                App.auth.close();
            }
        });
    }

    private void createClientDir(String username) {
        clientDir=Paths.get(ROOT_DIR+"\\"+username);
        if (!Files.exists(clientDir)) {
            try {
                Files.createDirectories(clientDir);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("Password or login error");
        alert.setContentText(" login or password are incorrect, try again");
        alert.showAndWait();
    }


    private void fileMessage(FileMessage message) throws IOException {
        Path file = Paths.get(clientDir + "\\" + message.getName());

        if (message.isFirstBatch()) {
            Files.deleteIfExists(file);

        }
        try (FileOutputStream os = new FileOutputStream(file.toFile(), true)) {
            os.write(message.getBytes(), 0, message.getEndByteNum());
        }

    }

    private void listMessage(ListMessage message) {
        runLater(() -> {
            listViewServer.getItems().clear();
            listViewServer.getItems().addAll(message.getFiles());
            refreshClient();
        });

    }


    public void uploadFile(ActionEvent actionEvent) throws IOException {

        sendFile(fileNameClient.getText());
        refreshClient();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = fileNameServer.getText();
        net.send(new FileRequest(fileName));

    }

    private void addViewListener(ListView<String> lv, TextArea ta) {
        lv.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    try {
                        ta.clear();
                        ta.appendText(lv.getSelectionModel().getSelectedItem());
                    } catch (NullPointerException ignored) {
                    }
                });
    }

    private void sendFile(String fileName) throws IOException {
        boolean isFirstBatch = true;
        Path filePath = Paths.get(clientDir + "\\" + fileName);
        System.out.println(clientDir + "\\" + fileName);

        long size = Files.size(filePath);
        try (FileInputStream is = new FileInputStream(filePath.toFile())) {
            int read;

            while ((read = is.read(buffer)) != -1) {
                FileMessage message = FileMessage.builder()
                        .bytes(buffer)
                        .name(filePath.getFileName().toString())
                        .size(size)
                        .isFirstBatch(isFirstBatch)
                        .isFinishBatch(is.available() <= 0)
                        .endByteNum(read)
                        .build();

                net.send(message);
                buffer = new byte[8192];
                isFirstBatch = false;

            }
            log.debug("package sent!!!");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void refreshClient() {
        listViewClient.getItems().clear();
        listViewClient.getItems().addAll(clientDir.toFile().list());
    }


}

