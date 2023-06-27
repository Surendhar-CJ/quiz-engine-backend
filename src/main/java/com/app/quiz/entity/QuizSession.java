package com.app.quiz.entity;

import java.util.List;
import java.util.Map;

public class QuizSession {

    private Long id;
    private Quiz quiz;
    private Question currentQuestion;
    private List<Question> servedQuestions;
    private Map<Question, List<Choice>> responses;

}
