package com.app.quiz.dto;

import com.app.quiz.entity.Feedback;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Response;
import com.app.quiz.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;


public record QuizDTO (
        Long id,
        Long userId,
        Topic topic,
        Feedback feedbackType,
        Boolean isCompleted,
        Integer questionsLimit,
        String difficultyLevel,
        Double finalScore,
        LocalDateTime createdAt,
        List<Question> servedQuestions,
        List<Response> responses
)

{}
