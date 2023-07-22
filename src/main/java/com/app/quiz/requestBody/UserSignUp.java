package com.app.quiz.requestBody;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSignUp {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
}
