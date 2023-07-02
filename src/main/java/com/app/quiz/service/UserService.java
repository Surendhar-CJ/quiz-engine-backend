package com.app.quiz.service;

import com.app.quiz.dto.UserDTO;
import com.app.quiz.entity.User;
import com.app.quiz.requestBody.UserLogin;

public interface UserService {

    UserDTO createUser(User user);

    UserDTO getUserById(Long id);

    /*User updateUser(Long id, User user);

    void deleteUserById(Long id);

   */
    User login(UserLogin userLogin);
}
