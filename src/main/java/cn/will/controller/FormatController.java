package cn.will.controller;

import cn.will.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;

/**
 * Created on 2018-01-09 11:03 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class FormatController {

    private Main system;

    public Main getSystem() {
        return system;
    }

    public void setSystem(Main system) {
        this.system = system;
    }

    @FXML
    private ProgressBar diskSizeBar;

    @FXML
    private TableView allocatedTableView;

    @FXML
    private Button addVolumeBtn;


    @FXML
    private void initialize(){
        diskSizeBar.setProgress(1);
        this.allocatedTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void newVolume(){
        system.newVolumeStage();
    }

    @FXML
    private void format() {

    }

    @FXML
    private void cancel(){
        system.closeFormatStage();
        system.showMainStage();
    }
}
