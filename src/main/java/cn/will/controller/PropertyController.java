package cn.will.controller;

import cn.will.file.FileAllocationTable;
import cn.will.file.FileControlBlock;
import cn.will.file.Memory;
import cn.will.tree.FileTreeNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created on 2018-01-10 2:40 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class PropertyController {

    @FXML
    private Label typeText;

    @FXML
    private Label descriptionText;

    @FXML
    private Label locationText;

    @FXML
    private Label sizeLabel;

    @FXML
    private Label physicsLocationText;

    @FXML
    private Label sizeOnDiskText;

    @FXML
    private Label createText;

    @FXML
    private Label modifiedText;

    @FXML
    private CheckBox readOnlyCheckBox;

    private FileTreeNode fileTreeNode;

    @FXML
    private void initialize(){
        initReadOnlyCheckBox();
    }

    private void initReadOnlyCheckBox(){
        this.readOnlyCheckBox.selectedProperty().addListener(
                (observable, oldValue, newValue) ->{
                    setReadOnly(newValue,fileTreeNode);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        System.out.println(mapper.writeValueAsString(fileTreeNode.getFcb()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

        );

        Memory.getInstance().updateFileTree();
        Memory.getInstance().updateFCB();
    }

    private void setReadOnly(boolean readOnly,FileTreeNode file){
        file.getFcb().setReadOnly(readOnly);
        if (file.isDir()){
            ArrayList<FileTreeNode> children = file.getChildren();
            for (int i = 0; i < children.size(); i++) {
                FileTreeNode child = children.get(i);
                //递归为该目录下的文件/目录设置读写权限
                setReadOnly(readOnly,child);
            }
        }
    }

    public void updateInfo(){
        if (null == fileTreeNode) {
            throw new NullPointerException("File Null");
        }
        typeText.setText(getTypeOfFile());
        descriptionText.setText(getFileDescription());
        locationText.setText(getFileLocation());
        createText.setText(getCreate());
        if (fileTreeNode.isDir()) {
            sizeLabel.setText("Contains :");
            sizeOnDiskText.setText(getContainFolder(fileTreeNode) + " folders, " + getContainFile(fileTreeNode)
                    +"files");
            modifiedText.setText(getCreate());
        } else {
            sizeOnDiskText.setText(getSizeOnDisk());
            modifiedText.setText(getLastModified());
            physicsLocationText.setText("#"+getFirstBlock() + " block");
        }
        FileControlBlock fcb = fileTreeNode.getFcb();
        readOnlyCheckBox.setSelected(fcb.isReadOnly());
    }

    private String getTypeOfFile(){
        String typeOfFile;
        if (fileTreeNode.isDir()) {
            typeOfFile = "File Folder";
        } else {
            typeOfFile = "File";
        }
        return typeOfFile;
    }

    private String getFileDescription(){
        String desc = "";
        desc = fileTreeNode.getCurrent();
        return desc;
    }

    private String getFileLocation(){
        String location;
        location = fileTreeNode.getAbsolutePath();
        return location.replaceFirst("/","");
    }

    private String getSizeOnDisk(){
        FileControlBlock fcb = fileTreeNode.getFcb();
        List<FileAllocationTable> fat = Memory.getInstance().loadFile(fcb.getiNode());
        return String.valueOf(fat.size()) +" block(s)";
    }

    private String getFirstBlock(){
        return String.valueOf(fileTreeNode.getFcb().getiNode());
    }

    private String getCreate(){
        long create = fileTreeNode.getFcb().getCreate();
        return new Date(create).toString();
    }

    private String getLastModified(){
        long modified = fileTreeNode.getFcb().getModified();
        return new Date(modified).toString();
    }

    private int getContainFolder(FileTreeNode folder){
        int num = 0;
        ArrayList<FileTreeNode> children = folder.getChildren();
        if (null == children || children.isEmpty()) {
            return 0;
        }
        for (int i = 0;i < children.size();i++){
            FileTreeNode child = children.get(i);
            if (child.isDir()) {
                num++;
                num = num + getContainFolder(child);
            }
        }
        return num;
    }

    private int getContainFile(FileTreeNode file){
        int num = 0;
        ArrayList<FileTreeNode> children = file.getChildren();
        if (null == children || children.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < children.size(); i++) {
            FileTreeNode child = children.get(i);
            if (child.isDir()){
                num = num + getContainFile(child);
            } else {
                num ++;
            }
        }
        return num;
    }

    public FileTreeNode getFileTreeNode() {
        return fileTreeNode;
    }

    public void setFileTreeNode(FileTreeNode fileTreeNode) {
        this.fileTreeNode = fileTreeNode;
    }
}
