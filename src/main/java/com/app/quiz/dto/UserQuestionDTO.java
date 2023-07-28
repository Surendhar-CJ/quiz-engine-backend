package com.app.quiz.dto;

import com.app.quiz.entity.Choice;

import java.util.List;

public record UserQuestionDTO (

    Long id,
    String topicName,
    String text,
    List<Choice> choices,
    String explanation

)
{}
