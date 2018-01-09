package cn.will;

import cn.will.controller.FormatController;
import cn.will.controller.LoginController;
import cn.will.controller.MainLayoutController;
import cn.will.controller.VolumeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created on 2018-01-07 5:46 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (null == loginStage) {
            initLoginStage();
        }
        if (null == mainStage) {
            initMainStage();
        }
        if (null == formatStage) {
            initFormatStage();
        }
//        showMainStage();
//        newVolumeStage();
        showFormatStage();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Stage loginStage;

    private Stage mainStage;

    private Stage formatStage;

    private void initLoginStage(){
        Stage stage = new Stage();
        stage.setResizable(false);
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/login.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image("img/user.png"));
        stage.setTitle("Login");
        stage.setScene(new Scene(root));

        LoginController controller = loader.getController();
        controller.setSystem(this);
        this.loginStage = stage;
    }

    private void initMainStage(){
        Stage stage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/main-layout.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image("img/filesystem.png"));
        stage.setTitle("Mock File System");
        stage.setScene(new Scene(root));

        MainLayoutController controller = loader.getController();
        controller.setSystem(this);
        this.mainStage = stage;
    }

    private void initFormatStage(){
        Stage stage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/format.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image("img/filesystem.png"));
        stage.setTitle("Format Disk");
        stage.setScene(new Scene(root));

        FormatController controller = loader.getController();
        controller.setSystem(this);
        stage.setResizable(false);
        this.formatStage = stage;
    }

    public void newVolumeStage(){
        Stage stage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/volume.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image("img/filesystem.png"));
        stage.setTitle("New Volume");
        stage.setScene(new Scene(root));

        VolumeController controller = loader.getController();
        controller.setSystem(this);
        stage.initOwner(formatStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void showMainStage(){
        if (null != mainStage) {
            mainStage.show();
        } else {
            throw new RuntimeException("null main stage");
        }
    }

    public void closeMainStage(){
        if (null != mainStage) {
            mainStage.close();
        } else {
            throw new RuntimeException("null main stage");
        }
    }

    public void showFormatStage() {
        if (null != formatStage) {
            formatStage.show();
        } else {
            throw new RuntimeException("null main stage");
        }
    }

    public void closeFormatStage() {
        if (null != formatStage) {
            formatStage.close();
        } else {
            throw new RuntimeException("null main stage");
        }
    }

    public void showLoginLayout(){
        if (null != loginStage){
            loginStage.show();
        }else {
            throw new RuntimeException("empty login stage");
        }
    }

    public void closeLoginWindow(){
        if (null != loginStage) {
            loginStage.close();
        } else {
            throw new RuntimeException("empty login stage");
        }
    }
}
