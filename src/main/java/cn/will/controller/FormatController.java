package cn.will.controller;

import cn.will.Main;
import cn.will.Resources;
import cn.will.Volume;
import cn.will.file.FileControlBlock;
import cn.will.file.Memory;
import cn.will.tree.FileTreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018-01-09 11:03 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class FormatController {

    private static final int MAX_BLOCK = 1024;

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

    private List<Volume> volumes;

    @FXML
    private Button addVolumeBtn;

    @FXML
    private void initialize(){
        diskSizeBar.setProgress(1.0/MAX_BLOCK);
        initTableView();
    }

    private void initTableView(){
        this.allocatedTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void newVolume(){
        system.showVolumeStage();
    }

    @FXML
    private void format() {
        //格式化分区信息
        List<Volume> volumes = formatVolume();
        //格式化FCB
        List<FileControlBlock> formattedFCB = new ArrayList<>();
        ArrayList<FileTreeNode> children = new ArrayList<>();

        FileTreeNode root = new FileTreeNode(null,"",true);
        for (int i = 0; i < volumes.size(); i++) {
            Volume volume = volumes.get(i);
            FileTreeNode child = new FileTreeNode(root,volume.getName(),true);
            FileControlBlock fcb = new FileControlBlock(volume.getName(),true,-2);
            long create = System.currentTimeMillis();
            fcb.setCreate(create);
            fcb.setModified(create);
            fcb.setOwner("system");

            child.setFcb(fcb);

            children.add(child);
            formattedFCB.add(fcb);
        }
        root.setChildren(children);
        Memory.getInstance().formatDisk(formattedFCB,root);
        system.closeFormatStage();
    }

    private List<Volume> formatVolume(){
        allocatedTableView.sort();
        List<Volume> volumes = allocatedTableView.getItems();
        int start = 2;
        Volume volume;
        //分配地址
        for (int i = 0; i < volumes.size(); i++) {
            volume = volumes.get(i);
            volume.setStart(start);
            start = start + volume.getSize();
        }
        save2External(volumes);
        return volumes;
    }

    private void save2External(List<Volume> volumes){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(Resources.VOLUME_LOCATION);
        try {
            mapper.writeValue(file,volumes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSizeBar(double value){
        diskSizeBar.setProgress(value);
        if (value >= 1){
            addVolumeBtn.setDisable(true);
        }
    }

    @FXML
    private void cancel(){
        system.closeFormatStage();
        system.showMainStage();
    }

    public void addVolume(Volume volume){
        allocatedTableView.getItems().add(volume);
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }
}
