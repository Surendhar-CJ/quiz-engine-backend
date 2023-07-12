package com.app.quiz.dto;

import com.app.quiz.entity.Feedback;
import com.app.quiz.entity.Topic;
import java.time.LocalDateTime;


public record QuizDTO (
    Long id,
    Long userId,
    Topic topic,
    Feedback feedbackType,
    Boolean isCompleted,
    Double finalScore,
    LocalDateTime createdAt
)

{}
