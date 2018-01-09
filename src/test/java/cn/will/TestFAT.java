package cn.will;

import cn.will.file.FileAllocationTable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018-01-08 1:55 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class TestFAT {

    private File storeFile;
    private ObjectMapper mapper;

    @Before
    public void before(){
        storeFile = new File(new File("").getAbsolutePath() + "\\src\\main\\resources\\store\\fat.json");
        mapper = new ObjectMapper();
    }

    @Test
    public void write2External(){
        List<FileAllocationTable> fat = new ArrayList<>();
        for (int i = 0; i < 1024; i++) {
            FileAllocationTable item = new FileAllocationTable(i,-1);
            fat.add(item);
        }
        try {
            mapper.writeValue(storeFile,fat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadFromExternal() {
        try {

            List<FileAllocationTable> fat;
            fat=mapper.readValue(storeFile,new TypeReference<List<FileAllocationTable>>(){});
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createNewFCB(){
    }
}
