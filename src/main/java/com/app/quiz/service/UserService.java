package com.app.quiz.service;

import com.app.quiz.entity.User;
import com.app.quiz.requestBody.UserLogin;

public interface UserService {

    User createUser(User user);

    User getUserById(Long id);

    /*User updateUser(Long id, User user);

    void deleteUserById(Long id);

   */
    User login(UserLogin userLogin);
}
