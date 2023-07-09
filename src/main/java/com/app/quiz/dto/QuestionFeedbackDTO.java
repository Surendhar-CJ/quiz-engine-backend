package com.app.quiz.dto;

import com.app.quiz.utils.FeedbackResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class QuestionFeedbackDTO {
    private QuestionDTO questionDTO;
    private FeedbackResponse feedbackResponse;
}
