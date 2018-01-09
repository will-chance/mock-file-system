package cn.will;

import cn.will.tree.FileTreeNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Created on 2018-01-07 4:50 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class Test {
    public static void main(String[] args) {
        //模拟一个多级文件目录
//        /
//        ├─dev
//        ├─share
//        ├─user1
//        │  ├─A
//        │  ├─B
//        │  └─C
//        │    └─Y
//        └─user2
        String path = new File("").getAbsolutePath() + "\\src\\main\\resources\\store\\filesystem.json";
        File filesystem = new File(path);
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(filesystem);
            FileTreeNode root = mapper.readValue(filesystem, FileTreeNode.class);
            System.out.println();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
