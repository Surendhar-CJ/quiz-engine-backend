package com.app.quiz.dto;

import java.util.List;
import java.util.Map;

public record UserQuizDTO (
    Long userId,
    String firstName,
    String lastName,
    String email,
    List<QuizDTO> quizList,
    Map<Long, Double> averageScoreByTopic
)

{}
