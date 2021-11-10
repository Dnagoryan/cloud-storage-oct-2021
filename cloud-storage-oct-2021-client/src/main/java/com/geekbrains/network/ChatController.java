package com.geekbrains.network;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.navigation.FileMessage;
import com.geekbrains.model.navigation.FileRequest;
import com.geekbrains.model.navigation.ListMessage;
import com.geekbrains.network.Net;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;

@Slf4j
public class ChatController implements Initializable {


    private Path currentDir;
    public ListView<String> listViewClient;
    public TextArea absolutPathClient;
    public TextArea fileNameClient;

    public ListView<String> listViewServer;
    public TextArea absolutPathServer;
    public TextArea fileNameServer;


    public Button upload;
    public Button download;
    private ObjectDecoderInputStream dis;
    private ObjectEncoderOutputStream dos;
    public static Net net;
    private byte[] buffer;
    private File clintDir;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buffer = new byte[8192];
        currentDir = Paths.get("client");
        clintDir = new File("client");
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


        }
    }

    private void fileMessage(FileMessage message) throws IOException {
        Path file = Paths.get(currentDir.toAbsolutePath() + "\\" + message.getName());
        //      System.out.println(currentDir.resolve(message.getName()));
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

//    private List<String> getFilesInCurrentDir() throws IOException {
//        return Files.list(currentDir).map(p -> p.getFileName().toString())
//                .collect(Collectors.toList());
//    }


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
        Path filePath = Paths.get(currentDir.toAbsolutePath() + "\\" + fileName);
        System.out.println(currentDir.toAbsolutePath() + "\\" + fileName);

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
        listViewClient.getItems().addAll(currentDir.toFile().list());
        System.out.println("ОБНОВИЛО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(Arrays.toString(currentDir.toFile().list()));
    }
}