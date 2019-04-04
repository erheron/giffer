 package com.poproject.app;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;


 public class MainApp extends Application {

    static Desktop desktop = Desktop.getDesktop();
    static Scene mainScene;
    static Stage mainWindow;

    private MainController mainController;
    @Override
    public void start(Stage primaryStage) throws Exception{
        final BooleanProperty firstTime = new SimpleBooleanProperty(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/form.fxml"));
        AnchorPane rootAnchorPane = loader.load();
        mainController = loader.getController();
        mainScene = new Scene(rootAnchorPane, 1000,800);
        mainScene.getStylesheets().add(getClass().getResource("/css/mainTheme.css").toExternalForm());
        mainWindow = new Stage();
        mainWindow.setScene(mainScene);
        mainWindow.setTitle("GIFFER - create your GIF!");
        mainWindow.show();
        mainController.about = new File("about/About.txt");
    }
    public static void main(String[] args) {
        launch(args);
    }

}

