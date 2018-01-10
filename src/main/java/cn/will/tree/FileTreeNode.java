package cn.will.tree;

import cn.will.file.FileControlBlock;

import java.util.ArrayList;

/**
 * Created on 2018-01-07 9:46 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class FileTreeNode {
    public FileTreeNode() {
    }

    /**
     * new file
     * @param parent
     * @param current
     */
    public FileTreeNode(FileTreeNode parent, String current) {
        if (null != parent){
            this.parent = parent.getCurrent();
        }
        this.parentNode = parent;
        this.current = current;
        isDir = false;
    }

    public FileTreeNode(FileTreeNode parent,String current,boolean isDir) {
        if (null != parent){
            this.parent = parent.getCurrent();
        }
        this.parentNode = parent;
        this.current = current;
        this.isDir = isDir;
        if (isDir) {
            this.children = new ArrayList<>();
        }
    }

    private FileTreeNode parentNode;

    private String parent;

    private String current;

    private ArrayList<FileTreeNode> children;

    private boolean isDir;

    /**
     * fcb 索引
     */
    @Deprecated
    private int index;

    /**
     * 该文件/目录所指向的 FCB
     */
    private FileControlBlock fcb;

    public FileTreeNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(FileTreeNode parentNode) {
        this.parentNode = parentNode;
    }

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

    public ArrayList<FileTreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<FileTreeNode> children) {
        this.children = children;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public int getIndex() {
        return index;
    }

    public FileControlBlock getFcb() {
        return fcb;
    }

    public void setFcb(FileControlBlock fcb) {
        this.fcb = fcb;
    }

    public String getAbsolutePath(){
        return getAbsolutePath(this);
    }

    private String getAbsolutePath(FileTreeNode leaf){
        String path = leaf.getCurrent();
        if (leaf.getParentNode() != null){
            path = getAbsolutePath(leaf.getParentNode()) + "/" + path;
        }
        return path;
    }

    @Override
    public String toString() {
        return current;
    }
}
