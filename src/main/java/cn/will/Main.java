package cn.will;

import cn.will.controller.FormatController;
import cn.will.controller.UserController;
import cn.will.controller.MainLayoutController;
import cn.will.controller.VolumeController;
import cn.will.file.BitMap;
import cn.will.file.Memory;
import cn.will.utils.FileUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        //加载必要的文件
        memory =  Memory.getInstance();
        //磁盘 奇校验
        BitMap bitMap = memory.getBitMap();
        int checkDigit = bitMap.oddCheck();
        //磁盘校验不通过，表示磁盘损坏
        if (!bitMap.check(checkDigit)){
            //alert
            String msg = "Can not start the system.";
            ButtonType restore = new ButtonType("Restore", ButtonBar.ButtonData.YES);
            ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.FINISH);
            Alert alert = new Alert(Alert.AlertType.ERROR,msg,restore,close);
            alert.setHeaderText("File System Broken");
            alert.initOwner(mainStage);
            alert.initModality(Modality.WINDOW_MODAL);
            Optional<ButtonType> option = alert.showAndWait();
            ButtonType buttonType = option.get();
            if (buttonType.equals(restore)){
                restore();
            }
        } else {
            showLoginLayout();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Memory memory;

    private Stage loginStage;

    private Stage mainStage;

    private Stage formatStage;

    private Stage volumeStage;

    private Stage registerStage;

    private User user;

    private FormatController formatController;

    private List<Volume> volumes;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public Stage getFormatStage() {
        return formatStage;
    }

    public Stage getVolumeStage() {
        return volumeStage;
    }

    private Stage initLoginStage(){
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
        UserController controller = loader.getController();
        controller.setSystem(this);
        return stage;
    }

    private Stage initMainStage(){
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
        return stage;
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

        formatController = loader.getController();
        formatController.setSystem(this);
        volumes = formatController.getVolumes();
        stage.setResizable(false);
        this.formatStage = stage;
    }

    public void initVolumeStage(){
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
        controller.setVolumes(volumes);
        stage.initOwner(formatStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        volumeStage = stage;
    }

    private Stage initRegisterStage(){
        Stage stage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/user.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image("img/user.png"));
        stage.setTitle("Register");
        stage.setScene(new Scene(root));

        UserController controller = loader.getController();
        controller.setSystem(this);
        stage.initOwner(mainStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        return stage;
    }

    private Stage initPropertyStage(String filename){
        Stage stage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/property.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image("img/filesystem.png"));
        stage.setTitle(filename + " Property");
        stage.setScene(new Scene(root));

        stage.initOwner(mainStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        return stage;
    }

    public void showPropertyStage(){
        Stage stage = initPropertyStage("disk");
        stage.show();
    }

    public void showRegisterStage(){
        registerStage = initRegisterStage();
        registerStage.showAndWait();
    }

    public void closeRegisterStage() {
        if (null != registerStage) {
            registerStage.close();
        }
    }

    public void showVolumeStage(){
        if (null == volumeStage){
            initVolumeStage();
        }
        volumeStage.showAndWait();
    }

    public void closeVolumeStage(){
        if (null == volumeStage) {
            throw new NullPointerException();
        }
        volumeStage.close();
    }

    public void showMainStage(){
        if (null == mainStage) {
            mainStage = initMainStage();
        }
        mainStage.show();
    }

    public void newMainStage(){
        mainStage = initMainStage();
        mainStage.show();
    }

    public void closeMainStage(){
        if (null != mainStage) {
            mainStage.close();
        } else {
            throw new RuntimeException("null main stage");
        }
    }

    public void showFormatStage() {
        if (null == formatStage) {
            initFormatStage();
        }
        formatStage.show();
    }

    public void closeFormatStage() {
        if (null != formatStage) {
            formatStage.close();
        } else {
            throw new RuntimeException("null main stage");
        }
    }

    public void showLoginLayout(){
        loginStage = initLoginStage();
        loginStage.show();
    }

    public void closeLoginWindow(){
        if (null != loginStage) {
            loginStage.close();
        } else {
            throw new RuntimeException("empty login stage");
        }
    }

    public void addVolume(Volume volume){
        formatController.addVolume(volume);
    }

    public void updateDiskSizeBar(double value) {
        formatController.updateSizeBar(value);
    }

    public boolean restore(){
        List<String> necessaryFiles = new ArrayList<>();
        Collections.addAll(necessaryFiles,new String[]{
                "disk","fat.json","fcbs.json","filesystem.json","user.json","volume.json"
        });
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Store Folder");
        directoryChooser.setInitialDirectory(new File(Resources.BASE_DIR + "\\src\\main\\resources"));
        File dir = directoryChooser.showDialog(mainStage);
        if (null == dir){
            return false;
        }
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getName();
            if (necessaryFiles.contains(filename)){
                necessaryFiles.remove(filename);
            }
        }
        //检查必要的文件
        if (!necessaryFiles.isEmpty()){
            String lackFile = "";
            for (String filename : necessaryFiles) {
                lackFile = lackFile + filename + "\n";
            }
            String msg = "These necessary file cannot be found:\n" + lackFile;
            showRestoreFailAlert(msg);
            return false;
        }

        //复制文件
        for (int i=0;i<files.length;i++){
            File src = files[i];
            File dst = new File(Resources.STORE_DIR + src.getName());
            try {
                System.out.println(FileUtils.channelCopy(src,dst));
            } catch (Exception e) {
                e.printStackTrace();
                showRestoreFailAlert("Unknown reason");
            }
        }
        showRestoreSuccessAlert();
        return true;
    }

    private void showRestoreFailAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.INFORMATION,msg,
                new ButtonType("OK", ButtonBar.ButtonData.YES));
        alert.setHeaderText("Restore Fail");
        alert.initOwner(mainStage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    private void showRestoreSuccessAlert(){
        String msg = "";
        Alert alert = new Alert(Alert.AlertType.INFORMATION,msg,
                new ButtonType("restart",ButtonBar.ButtonData.YES));
        alert.setHeaderText("Restore Success");
        alert.initOwner(mainStage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}
