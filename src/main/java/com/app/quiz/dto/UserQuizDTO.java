package com.app.quiz.dto;

import com.app.quiz.utils.QuizResult;

import java.util.List;
import java.util.Map;

public record UserQuizDTO (
    Long userId,
    String firstName,
    String lastName,
    String email,
    List<QuizResult> quizList,
    Map<Long, Double> averageScoreByTopic
)

{}
