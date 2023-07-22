package com.app.quiz.service;

import com.app.quiz.dto.UserDTO;
import com.app.quiz.dto.UserQuizDTO;
import com.app.quiz.entity.User;
import com.app.quiz.requestBody.UserLogin;
import com.app.quiz.requestBody.UserSignUp;

public interface UserService {

    UserDTO createUser(UserSignUp userSignUp);

    UserQuizDTO getUserById(Long id);

    /*User updateUser(Long id, User user);

    void deleteUserById(Long id);

   */
    UserDTO login(UserLogin userLogin);
}
