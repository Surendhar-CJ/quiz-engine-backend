package com.app.quiz.utils;

import com.app.quiz.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class UserResponse {

    private User user;
    private String token;
}
