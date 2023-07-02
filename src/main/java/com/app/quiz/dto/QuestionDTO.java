package com.app.quiz.dto;

import com.app.quiz.entity.Choice;

import java.util.List;

public record QuestionDTO (
        Long id,
        String text,
        Double score,
        String questionType,
        String questionDifficultyLevel,
        List<Choice> choices
)
{ }
