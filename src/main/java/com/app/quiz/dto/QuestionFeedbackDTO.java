package com.app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class QuestionFeedbackDTO {
    private QuestionDTO questionDTO;
    private String feedback;

}
