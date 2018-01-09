package cn.will.file;

/**
 * Created on 2018-01-08 11:16 AM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc: 文件分配表
 */
public class FileAllocationTable {
    public FileAllocationTable() {
    }

    public FileAllocationTable(int id, int next) {
        this.id = id;
        this.next = next;
    }

    private int id;

    private int next;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
}
