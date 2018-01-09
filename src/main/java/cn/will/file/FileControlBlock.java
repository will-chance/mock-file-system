package cn.will.file;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created on 2018-01-07 2:20 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc: 文件控制块
 */
public class FileControlBlock implements Serializable{

    public FileControlBlock() {
    }

    public FileControlBlock(String name,boolean isDir){
        this.name = name;
        this.isDir = isDir;
    }

    public FileControlBlock(String name, boolean isDir, int iNode) {
        this.name = name;
        this.isDir = isDir;
        this.iNode = iNode;
    }

    /**
     * 绝对路径名
     * @example "/dev/wlan/eth0" 表示 根目录下的 dev目录下的 wlan目录的 eth0文件
     *
     */
    private String name;

    private boolean isDir;

    /**
     * 索引节点
     * 暂时先用做第一盘块索引
     */
    private int iNode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getiNode() {
        return iNode;
    }

    public void setiNode(int iNode) {
        this.iNode = iNode;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileControlBlock that = (FileControlBlock) o;
        return isDir == that.isDir &&
                iNode == that.iNode &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isDir, iNode);
    }
}
