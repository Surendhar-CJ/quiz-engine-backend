package com.app.quiz.controller;

import com.app.quiz.config.jwt.JWTService;
import com.app.quiz.config.jwt.TokenBlacklist;
import com.app.quiz.dto.UserDTO;
import com.app.quiz.dto.UserQuizDTO;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {


    private final UserService userService;
    private final JWTService jwtService;
    private final TokenBlacklist tokenBlacklist;

    @Autowired
    public UserController(UserService userService, JWTService jwtService, TokenBlacklist tokenBlacklist) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        UserDTO createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserQuizDTO> getUser(@PathVariable("userId") Long userId) {
        UserQuizDTO user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.FOUND);
    }


    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody UserLogin userLogin) {
         if(userLogin == null) {
             throw new IllegalArgumentException("User login object cannot be null");
         }

         UserDTO user = userService.login(userLogin);

         final String jwt = jwtService.generateToken(userLogin.getUsername());

         return new ResponseEntity<>(new UserResponse(user, jwt), HttpStatus.OK);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody String token) {
        tokenBlacklist.addTokenToBlacklist(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
