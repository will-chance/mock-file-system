package cn.will.controller;

import cn.will.Main;
import cn.will.Volume;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Created on 2018-01-10 12:02 AM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class VolumeController {
    private static final int MAX_BLOCK = 1024;

    private static int usedBlock = 2;

    private Main system;

    public Main getSystem() {
        return system;
    }

    public void setSystem(Main system) {
        this.system = system;
    }

    private List<Volume> volumes;

    @FXML
    private TextField nameField;

    @FXML
    private TextField sizeField;

    @FXML
    private void cancel(){

    }

    @FXML
    private void newVolume(){
        String volumeName = nameField.getText();
        int size = Integer.valueOf(sizeField.getText());
        Volume volume = new Volume(volumeName,size);
        usedBlock = usedBlock + size;
        //更新表的显示
        system.addVolume(volume);
        //关闭当前窗口
        system.getVolumeStage().close();
        system.updateDiskSizeBar(usedBlock*1.0/MAX_BLOCK);
    }

    @FXML
    private void allLeftSize(){
        sizeField.setText(String.valueOf(MAX_BLOCK - usedBlock));
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }
}
