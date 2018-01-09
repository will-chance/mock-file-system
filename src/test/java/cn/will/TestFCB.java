package cn.will;

import cn.will.file.FileControlBlock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created on 2018-01-08 3:12 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class TestFCB {

    private File storeFile;

    private ObjectMapper mapper;

    @Before
    public void before() {
        mapper = new ObjectMapper();
        storeFile = new File(new File("").getAbsolutePath()+"\\src\\main\\resources\\store\\fcbs.json");
    }

    public List<FileControlBlock> testLoadFromExternal() {
        List<FileControlBlock> fcbs = null;
        try {
            fcbs = mapper.readValue(storeFile,new TypeReference<List<FileControlBlock>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fcbs;
    }

    @Test
    public void testLoadFile(){
        String filename = "/dev/printer";
        List<FileControlBlock> fcbs = testLoadFromExternal();
        FileControlBlock result = testSearch(fcbs, filename);
        try {
            System.out.println(mapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public FileControlBlock testSearch(List<FileControlBlock> fcbs,String filename){
        FileControlBlock fcb = null;
        for (FileControlBlock i:fcbs) {
            if (i.getName().equals(filename)){
                fcb = i;
                break;
            }
        }
        return fcb;
    }

    @Test
    public void testCreateExistFile(){
        String newFileName = "/will/hello";
        List<FileControlBlock> fcbs = testLoadFromExternal();
        FileControlBlock fcb = testSearch(fcbs, newFileName);
        if (fcb != null) {
            //exist
            //create fail
            System.out.println("ERROR:file exist.create file fail.");
        }
    }

    @Test
    public void testCreateFile() {
        String filename = "/will/test.txt";
        List<FileControlBlock> fcbs = testLoadFromExternal();
        FileControlBlock fcb = testSearch(fcbs, filename);
        int nextIndex = fcbs.size();
        if (null == fcb) {
            //filename not exist,create new one
            FileControlBlock fcb1 = new FileControlBlock(filename,false,nextIndex);
            fcbs.add(fcb1);
            System.out.println("INFO:create file success");
        } else {
            System.out.println("ERROR:file exist.create file fail.");
        }
        write2File(fcbs);

    }

    private void write2File(List<FileControlBlock> fcbs){
        try {
            FileWriter writer = new FileWriter(storeFile);
            String s = mapper.writeValueAsString(fcbs);
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


