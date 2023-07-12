package com.app.quiz.utils;

import com.app.quiz.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class UserResponse {

    private UserDTO user;
    private String token;
}
