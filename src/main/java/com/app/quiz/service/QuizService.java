package com.app.quiz.service;

import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.requestBody.StartQuiz;

public interface QuizService {

    Quiz createQuiz(StartQuiz startQuiz);

    Question questionGenerator(Long quizId, Long topicId);
}
