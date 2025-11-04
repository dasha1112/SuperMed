package com.supermed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManagerClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загружаем экран входа вместо главного окна
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("SuperMed - Вход в систему");
        primaryStage.setScene(new Scene(root, 500, 700));
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}