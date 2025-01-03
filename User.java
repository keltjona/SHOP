package com.example.shop;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;

    private boolean manager;

    public User(String username, String password, boolean manager) {
        this.username = username;
        this.password = password;
        this.manager = manager;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setManager(boolean manager){
        this.manager = manager;
    }

    public boolean isManager() {
        return manager;
    }
}
