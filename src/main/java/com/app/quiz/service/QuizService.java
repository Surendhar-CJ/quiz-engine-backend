package com.app.quiz.service;

import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.requestBody.ConfigureQuiz;

public interface QuizService {

    Quiz createQuiz(ConfigureQuiz configureQuiz);

    Question startQuiz(Long quizId, Long topicId);

    Question nextQuestion(AnswerResponse answerResponse);
}
