package cn.will.tree;

import cn.will.Main;
import cn.will.Resources;
import cn.will.User;
import cn.will.Volume;
import cn.will.controller.MainLayoutController;
import cn.will.controller.PropertyController;
import cn.will.file.BitMap;
import cn.will.file.FileAllocationTable;
import cn.will.file.FileControlBlock;
import cn.will.file.Memory;
import javafx.animation.PauseTransition;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    private Main system;

    private TextField textField;

    private final ContextMenu menu = new ContextMenu();

    public FileTreeCellImpl(TextArea editArea,TilePane explorerPane,TextField pathField,User currentUser,Main system) {
        this.system = system;
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

            String absolutePath ;

            FileTreeNode newDir = new FileTreeNode(item,filename,true);
            item.getChildren().add(newDir);

            TreeItem<FileTreeNode> newDirNode = new TreeItem(newDir, Resources.getDirIcon(16));
            getTreeItem().setExpanded(true);
            getTreeItem().getChildren().add(newDirNode);

            //创建目录的 FCB
            absolutePath = item.getAbsolutePath() + "/" + filename;
            FileControlBlock fcb = new FileControlBlock(absolutePath,true,-1);
            fcb.setOwner(currentUser.getUsername());
//            memory.addFileControlBlock(fcb); todo check
            long create = System.currentTimeMillis();
            fcb.setCreate(create);
            fcb.setModified(create);
            //为该目录设置 FCB
            newDir.setFcb(fcb);

            memory.updateFCB();
            memory.updateFileTree();
            focusFileTree(newDirNode);
            openFolder(item);
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

            //寻找卷的盘块
            String volumeName = absolutePath.substring(1, 2);
            Volume volume = memory.getVolume(volumeName);

            int startAddress = volume.getStart();
            int endAddress = volume.getSize() + startAddress;
            //查找空闲盘块
            BitMap bitMap = memory.getBitMap();
            int spareBlock = bitMap.findSpareBlock(bitMap.getUsage(),startAddress,endAddress);
            if (-1 == spareBlock){
                //没有空闲盘块，磁盘已满
                return;
            }
            bitMap.useBlock(spareBlock);

            //创建文件节点,更新文件树
            FileTreeNode newFile = new FileTreeNode(item, filename);
            item.getChildren().add(newFile);

            //添加fcbs 并更新到外存中
            absolutePath = item.getAbsolutePath() + "/" + filename;
            FileControlBlock fcb = new FileControlBlock(absolutePath,false,spareBlock);
            //文件所属者
            fcb.setOwner(currentUser.getUsername());
            long create = System.currentTimeMillis();
            fcb.setCreate(create);
            fcb.setModified(create);
//            memory.addFileControlBlock(fcb); todo check

            //为该文件设置指向的 FCB
            newFile.setFcb(fcb);

            //更新 FAT 表
            memory.useFAT(spareBlock,-1);

            TreeItem newFileNode = new TreeItem(newFile,Resources.getFileIcon(16));
            getTreeItem().getChildren().add(newFileNode);
            getTreeItem().setExpanded(true);
            focusFileTree(newFileNode);
            openFolder(item);
        });
        return newMenu;
    }

    /**
     * 聚焦到新添加到的文件中
     * @param node
     */
    private void focusFileTree(TreeItem node) {
        //聚焦到新添加的文件树节点上
        getTreeView().requestFocus();
        getTreeView().getSelectionModel().select(node);
        PauseTransition p = new PauseTransition(Duration.millis(100));
        p.setOnFinished(event -> {
            getTreeView().edit(node);
        });
        p.play();
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
            Label filename = new Label(child.getCurrent());
            filename.setMaxWidth(50);
            VBox tile = new VBox(icon,filename);
            Tooltip tip = new Tooltip(child.getCurrent());
            Tooltip.install(tile,tip);
            tile.getStyleClass().add("file-cell");
            tile.setAlignment(Pos.CENTER);
            tile.setOnMouseClicked(event -> {
                MouseButton button = event.getButton();
                if (button.compareTo(MouseButton.PRIMARY) ==0){
                    int clickCount = event.getClickCount();
                    if (clickCount == 2){
                        if (child.isDir()){
                            openFolder(child);
                        }else {
                            openFile(child);
                        }
                    }
                }
            });
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

    //初始化属性窗口
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

    //打开属性窗口
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
            String volumeName = file.getFcb().getName().substring(1, 2);
            MainLayoutController.updateChart(volumeName);
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
        textField.requestFocus();
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
                textField.requestFocus();
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setContextMenu(menu);
                setText(getString());
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        FileTreeNode item = getItem();
        textField.setOnKeyReleased((KeyEvent t) -> {

            //取消编辑
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                return;
            }
            if (t.getCode() != KeyCode.ENTER){
                return;
            }

                //获取新的文件名并做空判断处理
                String newName = textField.getText();
                if (null == newName || "".equals(newName)){
                    Tooltip tip = new Tooltip("name cannot be empty");
                    Tooltip.install(textField,tip);
                    return;
                }

                //获取旧的文件名做缓存
                String oldName = item.getCurrent();

                //获取文件fcb
                FileControlBlock fcb = item.getFcb();
                //通过fcbs是否包含该fcb判断是否是新文件
                boolean isNew = !memory.getFcbs().contains(fcb);

                if (newName.equals(oldName) && !isNew){
//                    文件名没变万而且 不是新文件，直接返回
                    cancelEdit();
                    return;
                }


                //设置新的文件名
                item.setCurrent(newName);
                //查找是否冲突命名
                FileControlBlock sameNameFile;
                if (item.isDir()){
                    sameNameFile = memory.searchFile(item.getAbsolutePath(),true);
                } else {
                    sameNameFile = memory.searchFile(item.getAbsolutePath(), false);
                }



            //文件名冲突 提示弹窗
                if (sameNameFile!=null){
                    showConflictRenameAlert(item, newName, oldName);
                    return;
                }

                //不冲突


                //更新fcb信息
                fcb.setName(item.getAbsolutePath());
                commitEdit(item);

                //表示是新插入的文件。需要添加fcb
                if (isNew){
                    memory.addFileControlBlock(fcb);
                }

                //写回内存 同步信息
                memory.updateAll();

            //同步更新面板
            FileTreeNode parent = item.getParentNode();
            if (parent!=null){
                openFolder(parent);
            }

        });
    }

    //显示文件命名冲突弹窗
    private void showConflictRenameAlert(FileTreeNode item, String newName, String oldName) {
        item.setCurrent(oldName);
        //表示有重名文件 --> 弹窗
        String msg = newName + " already exist in this folder.";
        Alert alert = new Alert(Alert.AlertType.INFORMATION,msg,new ButtonType("OK", ButtonBar.ButtonData
                .YES));
        alert.setHeaderText("Conflict Rename");
        alert.initOwner(system.getMainStage());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

}
