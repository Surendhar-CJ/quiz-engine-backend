package com.app.quiz.requestBody;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigureQuiz {
    private Long userId;
    private Long topicId;
    private Long feedbackId;
    private Integer questionsLimit;
    private String difficultyLevel;
}
