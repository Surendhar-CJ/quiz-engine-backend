package com.app.quiz.requestBody;

import com.app.quiz.entity.Choice;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AnswerResponse {
    private Long quizId;
    private Long questionId;
    private int sequenceNumber;
    private List<Choice> answerChoices;
}
