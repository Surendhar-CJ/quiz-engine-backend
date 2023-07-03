package com.app.quiz.service.feedback;

import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.requestBody.AnswerResponse;

public interface  FeedbackService {

    String generateFeedback(Quiz quiz, Question question, AnswerResponse answerResponse);
}
