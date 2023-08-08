package com.app.quiz.dto;

import com.app.quiz.utils.QuizResult;

import java.util.List;
import java.util.Map;

public record UserProfileDTO (
    Long userId,
    String firstName,
    String lastName,
    String email,
    List<QuizResult> quizList,
    List<UserTopicDTO> topicsCreated,
    List<UserQuestionDTO> questionsCreated,
    List<UserFeedbackDTO> feedbacksReceived,
    Map<Long, Double> averageScoreByTopic,
    Map<Long, Double> averagePercentageByOtherUsersPerTopic
)

{}
