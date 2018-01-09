package cn.will.controller;

import cn.will.Main;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Created on 2018-01-10 12:02 AM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class VolumeController {
    private Main system;

    public Main getSystem() {
        return system;
    }

    public void setSystem(Main system) {
        this.system = system;
    }

    @FXML
    private TextField nameField;

    @FXML
    private TextField sizeField;

    @FXML
    private void cancel(){

    }

    @FXML
    private void newVolume(){
        //更新表的显示
    }

    @FXML
    private void allLeftSize(){
        sizeField.setText("1024");
    }
}
