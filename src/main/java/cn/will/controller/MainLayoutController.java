package cn.will.controller;

import cn.will.Main;
import cn.will.Resources;
import cn.will.User;
import cn.will.Volume;
import cn.will.file.BitMap;
import cn.will.file.FileAllocationTable;
import cn.will.file.FileControlBlock;
import cn.will.file.Memory;
import cn.will.tree.FileTreeCellImpl;
import cn.will.tree.FileTreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018-01-07 5:36 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class MainLayoutController {

    private Main system;

    private Memory memory;

    private FileTreeNode rootFile;

    @FXML
    private TextArea editArea;

    @FXML
    private ScrollPane previewPane;

    @FXML
    private VBox usagePane;

    @FXML
    private TilePane explorerPane;

    @FXML
    private TextField pathField;

    @FXML
    private ScrollPane rightPane;

    @FXML
    private void initialize(){
        memory = Memory.getInstance();
        initExplorerPane();
        initPreviewTree();
        initVolumeUsage();
        Insets margin = new Insets(5, 5, 5, 5);
        BorderPane.setMargin(previewPane,new Insets(2, 0, 2, 2));
        BorderPane.setMargin(rightPane,new Insets(2, 2, 2, 0));
    }

    private void initExplorerPane(){
    }

    private void initPreviewTree(){
        //从内存中读取文件树
        this.rootFile = memory.getRootFile();
        TreeItem root = generateFileTreeView(rootFile);

        TreeView<FileTreeNode> previewTree = new TreeView<>(root);//以 root 为根节点创建预览树
        previewTree.setShowRoot(false);
        previewTree.setEditable(true);
        previewTree.setCellFactory((TreeView<FileTreeNode> p)
                -> new FileTreeCellImpl(editArea,explorerPane,pathField,system.getUser()));

        previewPane.setContent(previewTree);
    }

    /**
     * 从持久化存储中构造出一个树
     * @param root
     * @return
     */
    private TreeItem<FileTreeNode> generateFileTreeView(FileTreeNode root){
        TreeItem<FileTreeNode> treeRoot;

        if (root.isDir()) {
            if (root.getFcb() != null && root.getFcb().getiNode() == -2) {
                treeRoot = new TreeItem<>(root,Resources.getVolumeIcon(16));
            } else {
                treeRoot = new TreeItem(root,Resources.getDirIcon(16));
            }
            //dir
            if (root.getChildren() != null) {
                //traverse sub dir
                for (FileTreeNode child:root.getChildren()) {
                    //对每个子目录/文件设置父目录，指向当前root节点
                    child.setParentNode(root);
                    TreeItem subDir = generateFileTreeView(child);
                    treeRoot.getChildren().add(subDir);
                }
            }
        } else {
            //file
            treeRoot = new TreeItem(root,Resources.getFileIcon(16));
        }

        return treeRoot;
    }

    private void initVolumeUsage(){
        PieChart chart = new PieChart();
        chart.setTitle("volume usage");
        chart.setPrefSize(200,200);

        List<Volume> volumes = loadVolumeInfo();

        ObservableList volumeData = FXCollections.observableArrayList();
        for (Volume volume:volumes) {
            volumeData.add(new PieChart.Data(volume.getName(),volume.getSize()));
        }

        chart.setData(volumeData);

        usagePane.getChildren().addAll(chart);
    }

    private List<Volume> loadVolumeInfo(){
        List<Volume> volumes = null;
        File volumeFile = new File(Resources.VOLUME_LOCATION);
        ObjectMapper mapper = new ObjectMapper();
        try {
            volumes = mapper.readValue(volumeFile, new TypeReference<List<Volume>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return volumes;
    }

    /**
     * 退出登录
     */
    @FXML
    private void logOut(){
        //退出之前先保存
        memory.updateAll();
        system.closeMainStage();
        system.showLoginLayout();
    }

    /***
     * 格式化磁盘
     */
    @FXML
    private void reformat() {
        system.closeMainStage();
        system.showFormatStage();
    }

    /**
     * 新加用户
     */
    @FXML
    private void newUser(){
        system.showRegisterStage();
    }

    @FXML
    private void saveFile(){
        long modified = System.currentTimeMillis();
        //读取文件内容 == 模拟读取文件内容到内存中
        String content = this.editArea.getText();
        //模拟出该文件需要多少个盘块
        int size = content.length();
        //获取当前正在编辑的文件的FCB
        FileControlBlock currentFile = memory.getCurrentEditFile();
        currentFile.setModified(modified);
        //获取该文件原来使用的FAT
        List<FileAllocationTable> usedFAT = memory.loadFile(currentFile.getiNode());
        int oldFATSize = usedFAT.size();

        BitMap bitMap = memory.getBitMap();
        int difference = size - oldFATSize;
        //if difference == 0 do nothing
        //原来所存储的盘块和现在的盘块一样多，就使用原来的那些盘块
        //不用更新 FAT 和 位视图

        if (difference < 0) {
            //保存文件后所需要的盘块减少,去除多余的盘块
            List<FileAllocationTable> cashFAT = new ArrayList<>();
            for(int i = oldFATSize - 1;i>size-1;i--){
                cashFAT.add(usedFAT.get(i));
            }
            //更新FAT,释放FAT 表项
            memory.freeFAT(cashFAT);
            //更新位视图。释放磁盘空间
            for (FileAllocationTable cash:cashFAT){
                bitMap.freeBlock(cash.getId());
            }
        } else if (difference > 0) {
            //保存文件后所需要的盘块增多,加入多的盘块
            //寻找足够多的盘块来保存该文件
            boolean enough = true;
            //还需要 difference 个盘块
            int[] blocks = new int[difference];

            int tmpBlock;
            for (int i = 0; i < difference; i++) {
                tmpBlock = bitMap.findSpareBlock(bitMap.getUsage());
                if (tmpBlock == -1) {
                    //缺少块数>=1。磁盘空间不足，文件保存失败
                    enough =false;
                    break;
                }
                blocks[i] = tmpBlock;
                //更新对应的位视图
                bitMap.useBlock(tmpBlock);
            }
            //如果空间足够，写入对应 FAT
            if (enough){
                //原来已用的最后的一个 FAT 表项的下一项设置为 新增 FAT(盘块)的第一个 FAT(盘块)
                memory.useFAT(usedFAT.get(usedFAT.size() - 1).getId(),blocks[0]);
                memory.useFAT(blocks);
            }
        }

        //最后保存到外存中
        memory.updateAll();
    }

    @FXML
    private void closeFile(){
        editArea.clear();
        pathField.clear();
    }

    public Main getSystem() {
        return system;
    }

    public void setSystem(Main system) {
        this.system = system;
    }

}
