package com.app.quiz.service.feedback;

import com.app.quiz.entity.Feedback;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.requestBody.AnswerResponse;

import java.util.List;

public interface  FeedbackService {

    List<Feedback> getFeedbackTypes();

    String generateFeedback(Quiz quiz, Question question, AnswerResponse answerResponse);
}
