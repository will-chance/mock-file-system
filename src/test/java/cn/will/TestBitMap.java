package cn.will;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Random;

/**
 * Created on 2018-01-08 11:57 AM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class TestBitMap {

    private File storeFile;

    @Before
    public void init() {
        storeFile = new File(new File("").getAbsolutePath() + "\\src\\main\\resources\\store\\disk");
    }

    @Test
    public void testInitDisk(){
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

    @Test
    public void testRandomBitMap() {
        try {
            Random random = new Random(System.currentTimeMillis());
            FileWriter fileWriter = new FileWriter(storeFile);
            String buf;
            for (int i =0;i < 32;i++) {
                buf = "";
                for (int j = 0; j < 32; j++) {
                    int a = Math.abs(random.nextInt()%2);
                    buf = buf + String.valueOf(a);
                }
                fileWriter.write(buf);
                //换行
                fileWriter.write("\n");
            }
            fileWriter.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadDiskAvailable(){
        try {
            int used = 0;//已用盘块数
            int spare = 0;//空闲盘块数
            FileReader in = new FileReader(storeFile);
            BufferedReader reader = new BufferedReader(in);
            for (int i = 0; i < 32; i++) {
                String s = reader.readLine();
                char[] chars = s.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    if (chars[j] == '1') {
                        used ++;
                    } else if (chars[j] == '0'){
                        spare ++;
                    }
                }
            }
            int total = used + spare;
            System.out.println("total:" + total);
            String used1 = "used:" + used + "-" + ((used * 1.0)/total)* 100 +"%";
            String spare1 = "spare:" + spare + "-" + ((spare * 1.0)/total)* 100 +"%";
            System.out.println(used1);
            System.out.println(spare1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char[] testLoadBitMapFromExternal(){
        //32 * 32 = 1024 blocks
        char[] bitmap = new char[32 * 32];
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
        return  bitmap;
    }

    @Test
    public void testSaveBitMap2External(){
        char[] bitmap = testLoadBitMapFromExternal();
        save2External(bitmap);
    }

    private void save2External(char[] bitmap){
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

    @Test
    public void testFindSpareBlock(){
        char[] bitmap = testLoadBitMapFromExternal();
        int block = findFirstSpareBlock(bitmap);
        System.out.println("spare block id:" + block);
    }
    /**
     * 通过位视图查询空闲盘块
     */
    public int findFirstSpareBlock(char[] bitmap){
       int block = 0;
        for (int i = 0; i < 1024; i++) {
            if (bitmap[i] == '0') {
                block = i;
                break;
            }
        }
       return block;
    }
}
