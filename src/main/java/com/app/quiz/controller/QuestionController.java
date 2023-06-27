package com.app.quiz.controller;

import com.app.quiz.entity.Question;
import com.app.quiz.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<Question>> getAllQuestions(@PathVariable("topicId") Long topicId) {
       return new ResponseEntity<>( questionService.getAllQuestionsByTopic(topicId), HttpStatus.OK);
    }
}
