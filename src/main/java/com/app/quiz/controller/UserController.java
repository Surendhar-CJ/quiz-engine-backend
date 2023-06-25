package com.app.quiz.controller;

import com.app.quiz.config.jwt.JWTService;
import com.app.quiz.entity.User;
import com.app.quiz.requestBody.UserLogin;
import com.app.quiz.service.UserService;
import com.app.quiz.utils.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {


    private UserService userService;
    private JWTService jwtService;

    @Autowired
    public UserController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.FOUND);
    }


    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody UserLogin userLogin) {
         if(userLogin == null) {
             throw new IllegalArgumentException("User login object cannot be null");
         }

         User user = userService.login(userLogin);

         final String jwt = jwtService.generateToken(userLogin.getUsername());

         return new ResponseEntity<>(new UserResponse(user, jwt), HttpStatus.OK);
    }
}
