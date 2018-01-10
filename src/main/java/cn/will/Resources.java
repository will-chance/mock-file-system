package cn.will;

import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

/**
 * Created on 2018-01-07 11:04 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class Resources {

    public static final String BASE_DIR = new File("").getAbsolutePath();

    public static final String STORE_DIR = BASE_DIR + "\\src\\main\\resources\\store\\";

    public static final String BACKUP_DIR = BASE_DIR + "\\src\\main\\resources\\backup\\";

    public static final String VOLUME_LOCATION = STORE_DIR+"volume.json";

    public static final String USER_LOCATION = STORE_DIR+"user.json";

    public static final String IMG_DIR = BASE_DIR + "\\src\\main\\resources\\img\\";

    public static ImageView getDirIcon(int size){
        return new ImageView(new Image(Resources.class.getClassLoader().getResourceAsStream
                ("img/dir" + size +".png")));
    }

    public static ImageView getFileIcon(int size){
        return new ImageView(new Image(Resources.class.getClassLoader().getResourceAsStream
                ("img/file" + size + ".png")));
    }

    public static ImageView getVolumeIcon(int size){
        return new ImageView(new Image(Resources.class.getClassLoader().getResourceAsStream
                ("img/volume" + size + ".png")));
    }
}
