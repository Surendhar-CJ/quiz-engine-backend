package com.app.quiz.service;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.entity.Question;
import com.app.quiz.requestBody.QuestionAddition;

import java.util.List;

public interface QuestionService {

    List<Question> getAllQuestionsByTopic(Long topicId);

    QuestionDTO addQuestion(QuestionAddition questionAddition);

    void deleteQuestionById(Long questionId, Long userId);

}
