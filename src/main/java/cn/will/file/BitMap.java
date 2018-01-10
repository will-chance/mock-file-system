package cn.will.file;

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

    public BitMap(){
        this.usage = loadBitMap();
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
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(storeFile));
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

    /**
     * 给外部使用的接口
     */
    public void update(){
        save2External(this.usage);
    }

    /**
     * 查找空闲盘块
     * 顺序查找找到第一个.
     * @return 如果没有空闲盘块返回-1,否则返回对应的空闲盘块号
     */
    public int findSpareBlock(char[] bitmap){
        int block = -1;
        for (int i = 0; i < 1024; i++) {
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
    }

    public void format(){
        try {
            FileWriter fileWriter = new FileWriter(storeFile);
            char[] buf = new char[32];

            for (int i = 0; i < 31; i++) {
                buf[i] = '0';
            }
            //第0盘块默认已用,表示根目录
            for (int i = 0; i < 32; i++) {
                if (i == 0) {
                    buf[0] = '1';
                } else {
                    buf[0] = '0';
                }
                fileWriter.write(buf);
                fileWriter.write('\n');
            }
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
