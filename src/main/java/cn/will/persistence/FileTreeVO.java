package cn.will.persistence;

import cn.will.file.FileControlBlock;
import cn.will.tree.FileTreeNode;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 2018-01-08 10:07 AM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class FileTreeVO implements Serializable{
    private String parent;

    private String current;

    private ArrayList<FileTreeVO> children;

    private boolean isDir;

    /**
     * 索引节点
     */
    private FileControlBlock fcb;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public ArrayList<FileTreeVO> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<FileTreeVO> children) {
        this.children = children;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public FileControlBlock getFcb() {
        return fcb;
    }

    public void setFcb(FileControlBlock fcb) {
        this.fcb = fcb;
    }
}
