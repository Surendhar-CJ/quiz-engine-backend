package com.app.quiz.controller;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.entity.Question;
import com.app.quiz.requestBody.QuestionAddition;
import com.app.quiz.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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


    @PostMapping("/questions")
    public ResponseEntity<QuestionDTO> addQuestion(@RequestBody QuestionAddition questionAddition) {
        QuestionDTO question = questionService.addQuestion(questionAddition);
        return new ResponseEntity<>(question, HttpStatus.CREATED);
    }

    @DeleteMapping("/questions/{questionId}/{userId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("questionId") Long questionId, @PathVariable("userId") Long userId) {
        questionService.deleteQuestionById(questionId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
