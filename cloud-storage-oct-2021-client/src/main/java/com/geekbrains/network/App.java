package com.geekbrains.network;

import com.geekbrains.network.controllers.ChatController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends Application {
    public static Stage ps;
    public static  Stage auth;
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
        Parent parent = loader.load();
        primaryStage.setScene(new Scene(parent));
        ps = primaryStage;
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWING, new  EventHandler<WindowEvent>()
        {
            @SneakyThrows
            @Override
            public void handle(WindowEvent window)
            {
                auth = new Stage();
                auth.initModality(Modality.APPLICATION_MODAL);
                auth.initOwner(App.ps);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("auth.fxml"));
                Parent parent = loader.load();
                auth.setScene(new Scene(parent));
                auth.showAndWait();
            }
        });
        primaryStage.show();


    }
}
