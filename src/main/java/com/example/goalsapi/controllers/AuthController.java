package com.example.goalsapi.controllers;

import com.example.goalsapi.models.User;
import com.example.goalsapi.models.auth.AuthTokens;
import com.example.goalsapi.models.auth.CreateUserRequest;
import com.example.goalsapi.models.auth.LoginInfo;
import com.example.goalsapi.services.AuthService;
import com.example.goalsapi.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private UserService userService;

    @Autowired
    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping(path="/login", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public AuthTokens login (@RequestBody LoginInfo loginInfo) {
        return userService.login(loginInfo);
    }

    @PostMapping(path="/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestHeader String authorization){
        userService.logout(authorization);
    }
}
