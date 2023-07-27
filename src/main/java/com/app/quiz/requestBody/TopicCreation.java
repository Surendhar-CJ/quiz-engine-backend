package com.app.quiz.requestBody;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicCreation {
    private String topicName;
    private Long userId;
}
