package com.app.quiz.requestBody;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartQuiz {
    private Long userId;
    private Long topicId;

}
