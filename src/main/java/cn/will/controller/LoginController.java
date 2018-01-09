package cn.will.controller;

import cn.will.Main;
import cn.will.Resources;
import cn.will.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created on 2018-01-07 6:26 PM
 * Author: Bowei Chan
 * E-mail: bowei_chan@163.com
 * Project: mock-file-system
 * Desc:
 */
public class LoginController {
    private static final File USER_FILE = new File(Resources.STORE_DIR + "user.json");

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label tips;

    private Main system;

    private List<User> users;

    public LoginController() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            users = mapper.readValue(USER_FILE, new TypeReference<List<User>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void login(){
        String username = usernameField.getText();
        String password= passwordField.getText();
        if (null == username || "".equals(username)){
            tips.setText("empty username");
            return;
        }
        if (null == password || "".equals(password)){
            tips.setText("empty password");
            return;
        }
        User user = new User(username,password);
        if (users.contains(user)){
            //登录成功
            system.closeLoginWindow();
            system.showMainStage();
        } else {
            //登录失败
            tips.setText("incorrect username/password");
        }
    }

    public Main getSystem() {
        return system;
    }

    public void setSystem(Main system) {
        this.system = system;
    }
}
