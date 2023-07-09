package com.app.quiz.utils;

import com.app.quiz.entity.Choice;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class FeedbackResponse {
    private String result;
    private Choice correctAnswer;
    private String explanation;

}
