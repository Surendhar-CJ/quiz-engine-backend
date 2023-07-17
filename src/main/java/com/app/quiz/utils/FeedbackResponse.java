package com.app.quiz.utils;

import com.app.quiz.entity.Choice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public final class FeedbackResponse {
    private String result;
    private List<Choice> correctAnswer;
    private String explanation;

}
