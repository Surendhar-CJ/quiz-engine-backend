package com.app.quiz.service;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.dto.QuestionFeedbackDTO;
import com.app.quiz.dto.QuizDTO;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.requestBody.ConfigureQuiz;
import com.app.quiz.utils.FeedbackResponse;
import com.app.quiz.utils.QuizResult;

public interface QuizService {

    QuizDTO createQuiz(ConfigureQuiz configureQuiz);

    QuestionDTO startQuiz(Long quizId, Long topicId);

    QuestionDTO nextQuestion(AnswerResponse answerResponse);

    FeedbackResponse getFeedback(AnswerResponse answerResponse);

    QuizResult finishQuiz(Long quizId);
}
