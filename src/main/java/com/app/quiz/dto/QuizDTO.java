package com.app.quiz.dto;

import com.app.quiz.entity.Feedback;
import com.app.quiz.entity.Topic;


public record QuizDTO (
    Long id,
    Long userId,
    Topic topic,
    Feedback feedbackType,
    Boolean isCompleted,
    Double finalScore
)

{}
