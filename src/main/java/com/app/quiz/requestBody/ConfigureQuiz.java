package com.app.quiz.requestBody;

import com.app.quiz.entity.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigureQuiz {
    private Long userId;
    private Long topicId;
    private Long feedbackId;
}
