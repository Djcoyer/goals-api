package com.example.goalsapi.models.auth;

import lombok.Data;

@Data
public class LoginInfo {
    private String username;
    private String password;

    public LoginInfo() {}

    public LoginInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
