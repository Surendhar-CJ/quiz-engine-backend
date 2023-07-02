package com.app.quiz.dto;

import com.app.quiz.entity.Topic;


public record QuizDTO (
    Long quizId,
    Long topicId,
    Integer numberOfQuestions
)

{}
