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

    /**
     * 是否是目录
     */
    private boolean isDir;

    /**
     * 索引节点
     * 暂时先用做第一盘块索引
     */
    private int iNode;

    /**
     * 文件创建者
     */
    private String owner;

    /**
     * 所属组
     */
    private String group;

    /**
     * 读写控制位
     */
    private boolean readOnly;

    private boolean writeOnly;

    /**
     * 创建时间
     */
    private long create;

    /**
     * 最后修改时间
     */
    private long modified;

    public long getCreate() {
        return create;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setCreate(long create) {
        this.create = create;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

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
                create == that.create &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, isDir, iNode, create);
    }
}
