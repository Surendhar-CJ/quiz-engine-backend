package com.app.quiz.service;

import com.app.quiz.entity.Question;

import java.util.List;

public interface QuestionService {

    List<Question> getAllQuestionsByTopic(Long topicId);

}
