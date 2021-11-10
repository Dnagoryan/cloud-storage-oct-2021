package com.geekbrains.network;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends Application {
    public static Stage ps;
    @Override
    public void start(Stage primaryStage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth.fxml"));
//        Parent parent = loader.load();
//        primaryStage.setScene(new Scene(parent));
//        ps = primaryStage;
//        primaryStage.setOnCloseRequest(e -> System.exit(0));
//        primaryStage.show();
        Parent parent = FXMLLoader.load(getClass().getResource("auth.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();
    }
}
