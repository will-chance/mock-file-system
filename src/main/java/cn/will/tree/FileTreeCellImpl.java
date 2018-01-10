package cn.will.tree;

import cn.will.Resources;
import cn.will.User;
import cn.will.controller.PropertyController;
import cn.will.file.BitMap;
import cn.will.file.FileAllocationTable;
import cn.will.file.FileControlBlock;
import cn.will.file.Memory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018-01-08 12:02 AM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class FileTreeCellImpl extends TreeCell<FileTreeNode> {

    private User currentUser;

    private TextArea editArea;

    private TilePane explorerPane;

    private TextField pathField;

    private Memory memory;

    private TextField textField;

    private final ContextMenu menu = new ContextMenu();

    public FileTreeCellImpl(TextArea editArea,TilePane explorerPane,TextField pathField,User currentUser) {
        this.currentUser = currentUser;
        this.pathField = pathField;
        this.explorerPane = explorerPane;
        this.editArea = editArea;
        memory = Memory.getInstance();
        initMenu();
    }

    private void initMenu() {

        MenuItem deleteMenu = createDeleteMenu();
        MenuItem openMenu = createOpenMenu();
        MenuItem newDirMenu = createNewDirMenu();
        MenuItem newFileMenu = createNewFileMenu();
        MenuItem propertyMenu = createPropertyMenu();

        menu.getItems().add(openMenu);
        menu.getItems().add(newDirMenu);
        menu.getItems().add(newFileMenu);
        menu.getItems().add(deleteMenu);
        menu.getItems().add(propertyMenu);

    }

    private MenuItem createNewDirMenu(){
        MenuItem newDirMenu = new MenuItem("new folder");
        newDirMenu.setOnAction((ActionEvent t) -> {
            FileTreeNode item = getItem();

            if (!item.isDir()){
                return;
            }

            String filename = "NewFolder";

            String absolutePath = item.getAbsolutePath()+"/"+filename;

            FileTreeNode newDir = new FileTreeNode(item,filename,true);
            item.getChildren().add(newDir);

            TreeItem<FileTreeNode> newDirNode = new TreeItem(newDir, Resources.getDirIcon(16));
            getTreeItem().setExpanded(true);
            getTreeItem().getChildren().add(newDirNode);

            //创建目录的 FCB
            FileControlBlock fcb = new FileControlBlock(absolutePath,true,-1);
            fcb.setOwner(currentUser.getUsername());
            memory.getFcbs().add(fcb);
            long create = System.currentTimeMillis();
            fcb.setCreate(create);
            fcb.setModified(create);
            //为该目录设置 FCB
            newDir.setFcb(fcb);

            memory.updateFCB();
            memory.updateFileTree();
        });
        return newDirMenu;
    }

    private MenuItem createNewFileMenu(){
        MenuItem newMenu;
        //新建文件
        newMenu = new MenuItem("new file");
        newMenu.setOnAction((ActionEvent t)->{
            String filename = "NewFile";
            FileTreeNode item = getItem();
            String absolutePath = item.getAbsolutePath() + "/" + filename;

            //查重名文件
            FileControlBlock file = memory.searchFile(absolutePath,false);
            if (file != null) {
                //todo
                return;
            }

            //查找空闲盘块
            BitMap bitMap = memory.getBitMap();
            int spareBlock = bitMap.findSpareBlock(bitMap.getUsage());
            if (-1 == spareBlock){
                //没有空闲盘块，磁盘已满
                return;
            }
            bitMap.useBlock(spareBlock);

            //创建文件节点,更新文件树
            FileTreeNode newFile = new FileTreeNode(item, filename);
            item.getChildren().add(newFile);

            //添加fcbs 并更新到外存中
            FileControlBlock fcb = new FileControlBlock(absolutePath,false,spareBlock);
            //文件所属者
            fcb.setOwner(currentUser.getUsername());
            long create = System.currentTimeMillis();
            fcb.setCreate(create);
            fcb.setModified(create);
            memory.addFileControlBlock(fcb);

            //为该文件设置指向的 FCB
            newFile.setFcb(fcb);

            //更新 FAT 表
            memory.useFAT(spareBlock,-1);

            TreeItem newFileNode = new TreeItem(newFile,Resources.getFileIcon(16));
            getTreeItem().getChildren().add(newFileNode);
            getTreeItem().setExpanded(true);
            memory.updateAll();
        });
        return newMenu;
    }

    private MenuItem createOpenMenu(){
        MenuItem openMenu = new MenuItem("open");
        openMenu.setOnAction((ActionEvent t) ->{
            FileTreeNode item = getItem();
            if (item.isDir()) {
                openFolder(item);
            } else {
                openFile(item);
            }
        });
        return openMenu;
    }

    private void openFolder(FileTreeNode item) {
        //清空原有的。重新绘制新的
        explorerPane.getChildren().clear();
        //1.open dir --> explore folder
        //获取到该目录的子目录或文件
        ArrayList<FileTreeNode> children = item.getChildren();
        if (null == children || children.isEmpty()){
            updatePathView(item.getAbsolutePath());
            return;
        }
        ObservableList tiles = FXCollections.observableArrayList();
        for (int i = 0; i < children.size(); i++) {
            FileTreeNode child = children.get(i);
            ImageView icon;
            if (child.isDir()){
                icon = Resources.getDirIcon(48);
            } else {
                icon = Resources.getFileIcon(48);
            }
            VBox tile = new VBox(icon,new Text(child.getCurrent()));
            tile.setAlignment(Pos.CENTER);
            tiles.add(tile);
        }
        //重新绘制
        explorerPane.getChildren().addAll(tiles);
        updatePathView(item.getAbsolutePath().replaceFirst("/",""));
    }

    private void openFile(FileTreeNode item) {
        //2.open file --> explorer folder and put file content 2 edit area
        //获取文件对应的FCB，从而得知该文件的信息
        FileControlBlock fcb = item.getFcb();
        //设置当前正处于编辑的文件
        memory.setCurrentEditFile(fcb);
        //获取第一盘块
        int inode = fcb.getiNode();
        List<FileAllocationTable> fileFat = memory.loadFile(inode);
        String content = "";
        //新建的文件空内容占用一个盘块
        //文件内容中一个1表示该文件占用一个盘块
        for (int i = 0; i < fileFat.size(); i++) {
            content = content + "1";
        }
        this.editArea.setText(content);
        updatePathView(item.getAbsolutePath().replaceFirst("/",""));
    }

    private MenuItem createPropertyMenu(){
        MenuItem menuItem = new MenuItem("properties");
        menuItem.setOnAction((ActionEvent t)->{
            FileTreeNode item = getItem();
            showPropertyStage(item);
        });
        return menuItem;
    }

    private Stage initPropertyStage(FileTreeNode file){
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
        stage.setTitle(file.getCurrent() + " Properties");
        stage.setScene(new Scene(root));

        PropertyController controller = loader.getController();
        controller.setFileTreeNode(file);
        controller.updateInfo();

        stage.initOwner(null);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        return stage;
    }

    public void showPropertyStage(FileTreeNode file){
        Stage stage = initPropertyStage(file);
        stage.show();
    }

    private void updatePathView(String path){
        this.pathField.setText(path);
    }

    private MenuItem createDeleteMenu(){
        //删除文件/目录
        MenuItem deleteMenu = new MenuItem("delete");
        deleteMenu.setOnAction((ActionEvent t) ->{
            TreeItem<FileTreeNode> item = getTreeView().getSelectionModel().getSelectedItem();
            //从预览视图中删除
            item.getParent().getChildren().remove(item);
            //真正的删除，并保存到外存中
            deleteFile(item.getValue());
        });
        return deleteMenu;
    }

    private void deleteFile(FileTreeNode file){
        if (file.isDir() && file.getChildren() != null){
            //该目录下有目录/文件，递归删除子目录/子文件
            ArrayList<FileTreeNode> children = file.getChildren();
            for (int i = 0;i < children.size();i++) {
                deleteFile(children.get(i));
            }
        } else {
            //如果是文件，需要删除文件所占用的 FAT 表项和对应的磁盘空间
            int firstFAT = file.getFcb().getiNode();
            List<FileAllocationTable> fileFAT = memory.loadFile(firstFAT);
            //释放对应的磁盘空间
            BitMap bitMap = memory.getBitMap();
            for (int i = 0;i < fileFAT.size();i++){
                bitMap.freeBlock(fileFAT.get(i).getId());
            }
            //释放对应的 FAT 表项
            memory.freeFAT(fileFAT);
        }
        //删除对应的 FCB
        memory.getFcbs().remove(file.getFcb());
        //从文件树中删除
        file.getParentNode().getChildren().remove(file);
        memory.updateAll();
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        setText(null);
        textField.setText(getString());
        setGraphic(textField);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().getCurrent());
        setGraphic(getTreeItem().getGraphic());
    }

    @Override
    protected void updateItem(FileTreeNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setContextMenu(menu);
                setText(getString());
                setGraphic(getTreeItem().getGraphic());
                if (!getTreeItem().isLeaf()){
                    //非叶子节点,即目录节点可新建文件/目录
                }
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        FileTreeNode item = getItem();
        textField.setOnKeyReleased((KeyEvent t) -> {
            if (t.getCode() == KeyCode.ENTER) {
                item.setCurrent(textField.getText());
                //todo 查找当前目录中的重名文件
                FileControlBlock fcb = item.getFcb();
                fcb.setName(item.getAbsolutePath());
                commitEdit(item);
                memory.updateAll();
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

}
