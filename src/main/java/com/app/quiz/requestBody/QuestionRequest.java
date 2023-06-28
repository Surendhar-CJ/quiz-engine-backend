package com.app.quiz.requestBody;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionRequest {
    private Long quizId;
    private Long topicId;
}
