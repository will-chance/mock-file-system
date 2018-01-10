package cn.will.file;

import cn.will.Volume;

import java.io.*;

/**
 * Created on 2018-01-08 5:31 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc: 位视图
 */
public class BitMap {
    /**
     * 位视图的持久化存储文件
     */
    private final File storeFile = new File(new File("").getAbsolutePath() + "\\src\\main\\resources\\store\\disk");

    /**
     * 磁盘的使用情况
     * 用于标记空闲盘块
     * 1 表示已使用;
     * 0 表示空闲，可使用;
     */
    private char[] usage;

    private int usedBlock;

    public BitMap(){
        this.usage = loadBitMap();
        usedBlock = calcUsedBlock(usage);
    }

    public BitMap(char[] usage) {
        this.usage = usage;
    }

    public char[] getUsage(){
        if (null == usage) {
            usage = loadBitMap();
        }
        return this.usage;
    }

    /**
     * 加载位视图
     * 0-1023 分别表示对应盘块
     * @return
     */
    protected char[] loadBitMap(){
        char[] bitmap = new char[1024];
        try {
            FileReader in = new FileReader(storeFile);
            BufferedReader reader = new BufferedReader(in);
            String str = "";
            for (int i = 0; i < 32; i++) {
                str = str + reader.readLine();
            }
            bitmap = str.toCharArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 保存位视图到磁盘中
     * @param bitmap
     */
    protected void save2External(char[] bitmap){
        save2External(bitmap,storeFile);
    }

    protected void save2External(char[] bitmap,File file){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < 32; i++) {
                char[] buf = new char[32];
                for (int j = i*32,k=0; j < (i+1)*32; j++,k++) {
                    buf[k] = bitmap[j];
                }
                writer.write(buf);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backup(File backupFile){
        save2External(usage,backupFile);
    }

    /**
     * 给外部使用的接口
     */
    public void update(){
        int bit = oddCheck();
        usage[1] = bit == 1?'1':'0';
        save2External(this.usage);
    }

    public int findSpareBlock(char[] bitmap){
        return findSpareBlock(bitmap,2,1024);
    }

    /**
     * 查找空闲盘块
     * 顺序查找找到第一个.
     * @return 如果没有空闲盘块返回-1,否则返回对应的空闲盘块号
     */
    public int findSpareBlock(char[] bitmap,int start,int end){
        int block = -1;
        //从第二位开始寻找
        //第一位始终唯一，用于表示磁盘元信息
        //第二位模拟做校验
        for (int i = start; i < end; i++) {
            if (bitmap[i] == '0') {
                block = i;
                break;
            }
        }
        return block;
    }

    /**
     * 通过盘块号使用某个盘块
     * @param id
     */
    public void useBlock(int id){
        if (id<0 || id >= 1024) {
            throw new IndexOutOfBoundsException("#block must be [0,1023]");
        }
        this.usage[id] = '1';
        usedBlock++;
    }

    /**
     * 通过盘块号释放磁盘空间
     * @param id
     */
    public void freeBlock(int id){
        if (id<0 || id>= 1024){
            throw new IndexOutOfBoundsException("#block must be [0,1023]");
        }
        this.usage[id] = '0';
        usedBlock--;
    }

    public void format(){
        try {
            FileWriter fileWriter = new FileWriter(storeFile);
            char[] buf = new char[32];

            for (int i = 0; i < 31; i++) {
                buf[i] = '0';
            }
            {//第一行特殊写。第0块用于表示磁盘信息了。第1块模拟磁盘校验，做校验位
                buf[0] = buf[1] = '1';
                fileWriter.write(buf);
                fileWriter.write('\n');
                buf[0] = buf[1] = '0';
            }
            //第0盘块默认已用,表示根目录
            for (int i = 1; i < 32; i++) {
                fileWriter.write(buf);
                fileWriter.write('\n');
            }
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calcUsedBlock(char[] bitmap) {
        int num = 0;
        for (int i = 2; i < bitmap.length; i++) {
            if (bitmap[i] == '1') num++;
        }
        return num;
    }

    public int calcVolumeUsedBlock(Volume volume){
        int num = 0;
        int startAddr = volume.getStart();
        int endAddr = volume.getSize() + startAddr;
        for (int i = startAddr; i < endAddr; i++) {
            if (usage[i] == '1'){
                num++;
            }
        }
        return num;
    }

    /**
     * 奇校验
     * @return 返回需要填入的校验值
     */
    public int oddCheck() {
        return usedBlock%2 == 0?1:0;
    }

    /**
     * 查看现有的校验位和应该存在的校验位是否一致
     * @param checkDigit
     * @return true 为一致。false 表示磁盘损坏
     */
    public boolean check(int checkDigit){
        int bit = usage[1] == '1' ? 1:0;
        return checkDigit == bit;
    }
}
