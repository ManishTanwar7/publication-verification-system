package com.research.pubmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/research/pubmanager/view/Dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 750);
            primaryStage.setTitle("Research Publication Verification & Management System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("❌ Failed to load main dashboard FXML view.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
