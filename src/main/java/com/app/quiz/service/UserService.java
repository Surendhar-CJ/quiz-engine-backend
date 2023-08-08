package com.app.quiz.service;

import com.app.quiz.dto.UserDTO;
import com.app.quiz.dto.UserProfileDTO;
import com.app.quiz.requestBody.UserLogin;
import com.app.quiz.requestBody.UserSignUp;

public interface UserService {

    UserDTO createUser(UserSignUp userSignUp);

    UserProfileDTO getUserById(Long id);

    UserDTO login(UserLogin userLogin);
}
