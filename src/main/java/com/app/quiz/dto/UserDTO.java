package com.app.quiz.dto;

import com.app.quiz.entity.Quiz;

import java.util.List;

public record UserDTO (

        Long id,
        String firstName,
        String lastName,
        String email
)
{ }
