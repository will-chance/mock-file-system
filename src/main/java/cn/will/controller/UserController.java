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
public class UserController {
    private static final File USER_FILE = new File(Resources.STORE_DIR + "user.json");

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPwdField;

    @FXML
    private Label tips;

    @FXML
    private TextField rUsernameField;

    @FXML
    private PasswordField rPasswordField;

    @FXML
    private Label rTips;

    private Main system;

    private List<User> users;

    public UserController() {
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
            system.setUser(user);
            system.newMainStage();
        } else {
            //登录失败
            tips.setText("incorrect username/password");
        }
    }

    /**
     * 新加用户
     */
    @FXML
    public void register(){
        String username = ckUsername();
        if (null == username){
            return;
        }
        String pwd = null;
        if (!ckPwd()) {
            return;
        }
        pwd = rPasswordField.getText();
        User newUser = new User(username,pwd);
        users.add(newUser);
        save2External();
        system.closeRegisterStage();
    }

    private String ckUsername(){
        String username = rUsernameField.getText();
        if (null == username || "".equals(username)) {
            rTips.setText("username should not be empty");
            return null;
        }
        for (int i = 0; i < users.size(); i++) {
            if (username.equals(users.get(i).getUsername())){
                rTips.setText("user exist");
                return null;
            }
        }
        return username;
    }

    private boolean ckPwd(){
        String pwd = rPasswordField.getText();
        String rePwd = rPasswordField.getText();
        if (null == pwd || "".equals(pwd)){
            rTips.setText("password should be no empty");
            return false;
        }
        if (null == rePwd || "".equals(rePwd)){
            rTips.setText("repeat password should not be empty");
            return false;
        }
        if (!pwd.equals(rePwd)){
            rTips.setText("two password not match");
            return false;
        }
        return true;
    }

    private void save2External(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(USER_FILE,users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Main getSystem() {
        return system;
    }

    public void setSystem(Main system) {
        this.system = system;
    }
}
