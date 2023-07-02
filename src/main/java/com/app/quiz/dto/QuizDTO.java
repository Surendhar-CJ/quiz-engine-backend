package com.app.quiz.dto;

import com.app.quiz.entity.Topic;
import com.app.quiz.utils.FeedbackType;


public record QuizDTO (
    Long id,
    Long userId,
    Topic topic,
    FeedbackType feedbackType,
    Boolean isCompleted,
    Double finalScore
)

{}
