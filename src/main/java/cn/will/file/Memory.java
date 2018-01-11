package cn.will.file;

import cn.will.Resources;
import cn.will.User;
import cn.will.Volume;
import cn.will.persistence.FileTreeVO;
import cn.will.tree.FileTreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018-01-08 5:58 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:模拟内存
 */
public class Memory {

    private static Memory object;

    private Memory() {
        if (this.fat == null){
            this.fat = loadFAT();
        }
        if (this.fcbs == null){
            this.fcbs = loadFCBs();
        }
        if (this.rootFile ==null){
            this.rootFile = serialize2FileTreeNode(loadFileTree());
        }
        if (bitMap == null){
            this.bitMap = new BitMap();
        }
    }

    public static Memory getInstance(){
        if (null == object) {
            object = new Memory();
        }
        return object;
    }

    private static final String BASE_Dir = new File("").getAbsolutePath()
            + "\\src\\main\\resources\\store";
    private static final String FCB_LOCATION = BASE_Dir + "\\fcbs.json";
    private static final String FAT_LOCATION = BASE_Dir + "\\fat.json";
    private static final String FILE_TREE_LOCATION = BASE_Dir + "\\filesystem.json";
    private static final int MIN_FAT_ID = 0;
    private static final int MAX_FAT_ID = 1023;


    private final File fcbFile = new File(FCB_LOCATION);

    private final File fatFile = new File(FAT_LOCATION);

    private final File treeFile = new File(FILE_TREE_LOCATION);

    private final ObjectMapper mapper = new ObjectMapper();

    private List<Volume> volumes;

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }
    /****************************文件树*******************************/
    /**
     * 文件树的根节点
     */
    private FileTreeNode rootFile;

    /**
     * 从外存中加载文件树
     * @return
     */
    public FileTreeVO loadFileTree(){
        FileTreeVO root = null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            //转换成实体对象
            root = mapper.readValue(treeFile,FileTreeVO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * 转换成具有引用的对象
     * @param node
     * @return
     */
    private FileTreeNode serialize2FileTreeNode(FileTreeVO node) {
        FileTreeNode root = new FileTreeNode();
        //复制属性值，忽略children属性。因为两个对象的children的类型不同
        BeanUtils.copyProperties(node,root,"children");

        //如果该节点是目录节点。递归为该节点设置子目录
        ArrayList<FileTreeNode> children = new ArrayList<>();
        if (node.isDir() && node.getChildren() != null && !node.getChildren().isEmpty()) {
            //dir
            for (FileTreeVO i:node.getChildren()) {
                FileTreeNode child = serialize2FileTreeNode(i);
                children.add(child);
            }
        }
        root.setChildren(children);
        return root;
    }

    public void updateFileTree(){
        saveFileTree2Disk(this.rootFile);
    }

    /**
     * 将文件树保存到外存中.
     * todo 自动保存
     * @param node
     */
    public void saveFileTree2Disk(FileTreeNode node){
        saveFileTree2Disk(treeFile,node);
    }

    public void saveFileTree2Disk(File file,FileTreeNode node) {
        //序列化成可写到文件的对象
        FileTreeVO serializableFileTree = serialize2Persistence(node);
        //保存
        try {
            mapper.writeValue(file,serializableFileTree);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 持久化序列化
     * @param node
     * @return
     */
    private FileTreeVO serialize2Persistence(FileTreeNode node){
        FileTreeVO root = new FileTreeVO();
        ArrayList<FileTreeVO> children = new ArrayList<>();
        if (node.isDir() && node.getChildren()!= null && !node.getChildren().isEmpty()) {
            for (FileTreeNode i:node.getChildren()) {
                FileTreeVO child = serialize2Persistence(i);
                children.add(child);
            }
        }
        BeanUtils.copyProperties(node,root,"children");
        root.setChildren(children);
        return root;
    }

    public FileTreeNode getRootFile() {
        return rootFile;
    }

    /***************************************************************************/

    /**
     * 文件控制块表
     */
    private List<FileControlBlock> fcbs;

    public List<FileControlBlock> getFcbs() {
        return fcbs;
    }

    /**
     * 模拟加载fcb表。即查找目录
     * @return
     */
    private List<FileControlBlock> loadFCBs(){
        List<FileControlBlock> fcbs = null;
        try {
            fcbs = mapper.readValue(fcbFile,new TypeReference<List<FileControlBlock>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fcbs;
    }

    /**
     * 查找文件
     * @return
     */
    public FileControlBlock searchFile(String filename,boolean isDir){
        FileControlBlock file;
        if (null == filename || "".equals(filename)){
            throw new RuntimeException("ERROR:Empty Filename");
        }
        file = searchFile(this.fcbs,filename,isDir);
        return file;
    }

    /**
     * 顺序遍历 fcb 表来查找
     * todo 改进该查找算法
     * @param fcbs
     * @param filename
     * @return
     */
    private FileControlBlock searchFile(List<FileControlBlock> fcbs,String filename,boolean isDir){
        FileControlBlock file = null;
        for (FileControlBlock i:fcbs) {
            String name = i.getName();
            if (filename.equals(name)) {
                if (isDir == i.isDir()){
                    file = i;
                }
                break;
            }
        }
        return file;
    }

    /**
     * 添加新的文件控制块
     * @param file
     */
    public void addFileControlBlock(FileControlBlock file){
        this.fcbs.add(file);
    }

    /**
     * 保存 tcb 列表信息到外存中
     */
    protected void saveFCB2External(List<FileControlBlock> fcbs){
        writeList2External(fcbFile,fcbs);
    }

    public void saveFCB2External(File file,List<FileControlBlock> fcbs){
        writeList2External(file,fcbs);
    }

    public void updateFCB(){
        saveFCB2External(this.fcbs);
    }

    public void updateFCB(List<FileControlBlock> fcbs){
        saveFCB2External(fcbs);
    }

    /*************************FAT****************************/

    /**
     * FAT 表
     */
    private List<FileAllocationTable> fat;

    /**
     * 模拟加载 FAT 用于查找文件的盘块
     * @return
     */
    private List<FileAllocationTable> loadFAT(){
        List<FileAllocationTable> fat = null;
        try {
            fat=mapper.readValue(fatFile,new TypeReference<List<FileAllocationTable>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fat;
    }

    /**
     * 以物理块号使用某盘块
     * @param id
     * @param next
     */
    public void useFAT(int id,int next){
        FileAllocationTable fatItem = this.fat.get(id);
        fatItem.setNext(next);
    }

    public void useFAT(int[] blocks){
        int current;
        int next;
        for (int i = 0; i < blocks.length; i++) {
            //当前盘块
            current = blocks[i];
            if (i == blocks.length -1) {
                next = -1;
            } else {
                next = blocks[i + 1];
            }
            useFAT(current,next);
        }
    }

    public void freeFAT(int id){
        if (id < MIN_FAT_ID || id > MAX_FAT_ID){
            throw new RuntimeException("ERROR:#fat must between [" + MIN_FAT_ID +"," + MAX_FAT_ID + "]");
        }
        this.fat.get(id).setNext(-1);
    }

    public void freeFAT(List<FileAllocationTable> free) {
        for (FileAllocationTable i:free) {
            freeFAT(i.getId());
        }
    }

    public void saveFAT2External(List<FileAllocationTable> fat){
        writeList2External(fatFile,fat);
    }

    public void saveFAT2External(File file,List<FileAllocationTable> fat){
        writeList2External(file,fat);
    }

    private void writeList2External(File file,List list){
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            String s = mapper.writeValueAsString(list);
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateFAT(){
        saveFAT2External(this.fat);
    }

    public void updateFAT(List<FileAllocationTable> fat) {
        saveFAT2External(fat);
    }

    /**
     * 根据文件的第一个盘块获取整个文件的所有盘块
     * @param
     * @return
     */
    public List<FileAllocationTable> loadFile(int firstFatItem) {
        if (firstFatItem < 0 || firstFatItem > 1023) {
            throw new IndexOutOfBoundsException("ERROR:#FAT must between [0,1023]");
        }
        List<FileAllocationTable> file = new ArrayList<>();
        FileAllocationTable firstBlock = this.fat.get(firstFatItem);
        file.add(firstBlock);
        if (firstBlock.getNext() != -1) {
            // != -1 表示还有下一块
            List<FileAllocationTable> next = loadFile(firstBlock.getNext());
            file.addAll(next);
        }
        return file;
    }

    /***********************Bitmap***********************/
    private BitMap bitMap;

    public BitMap getBitMap() {
        return bitMap;
    }

    /****************************************************/

    public void updateAll(){
        saveFileTree2Disk(this.rootFile);
        saveFAT2External(this.fat);
        saveFCB2External(this.fcbs);
        this.bitMap.update();
    }

    public void backup(){
        String backupDir = Resources.BACKUP_DIR;

        //备份 FAT
        File fatBackup = new File(backupDir + "fat.json");
        saveFAT2External(fatBackup,fat);

        //备份文件树
        File fileTreeBackup = new File(backupDir + "filesystem.json");
        saveFileTree2Disk(fileTreeBackup,rootFile);

        File fcbBackup = new File(backupDir +"fcbs.json");
        saveFCB2External(fcbBackup,fcbs);

        //备份位视图
        File bitmapBackup = new File(backupDir + "disk");
        this.bitMap.backup(bitmapBackup);
    }

    /****************************************************/

    /**
     * 当前在文件编辑框的文件
     */
    private FileControlBlock currentEditFile;

    public FileControlBlock getCurrentEditFile() {
        return currentEditFile;
    }

    public void setCurrentEditFile(FileControlBlock currentEditFile) {
        this.currentEditFile = currentEditFile;
    }

    public void formatDisk(List<FileControlBlock> fcbs,FileTreeNode root){
        //1.更新用户文件
        formatUser();
        //2.更新位视图 bitmap
        bitMap.format();
        //3.更新 FAT
        formatFAT();
        //4.更新文件树
        saveFileTree2Disk(root);
        //5.更新 FCB
        saveFCB2External(fcbs);
    }

    private void formatUser(){
        File userFile = new File(Resources.USER_LOCATION);
        List<User> users = new ArrayList<>();
        users.add(new User("root","123456"));
        try {
            mapper.writeValue(userFile,users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void formatFAT(){
        List<FileAllocationTable> fat = new ArrayList<>();
        for (int i = 0; i < 1024; i++) {
            FileAllocationTable item = new FileAllocationTable(i,-1);
            fat.add(item);
        }
        try {
            mapper.writeValue(fatFile,fat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据卷名获取卷信息
     * @param volumeName
     * @return
     */
    public Volume getVolume(String volumeName){
        Volume volume = null;
        for (int i =0;i<volumes.size();i++){
            volume = volumes.get(i);
            if (!volume.getName().equals(volumeName)){
                volume = null;
            }else {
                break;
            }
        }
        return volume;
    }

}
