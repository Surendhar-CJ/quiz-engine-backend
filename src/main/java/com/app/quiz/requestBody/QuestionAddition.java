package com.app.quiz.requestBody;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class QuestionAddition {
    private Long userId;
    private Long topicId;
    private String subtopic;
    private String questionType;
    private String difficultyLevel;
    private String questionText;
    private Map<String, Boolean> choices;
    private String explanation;
    private Double score;

}
