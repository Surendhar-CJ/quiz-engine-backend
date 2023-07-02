package com.app.quiz.controller;


import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.dto.QuizDTO;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.requestBody.ConfigureQuiz;
import com.app.quiz.requestBody.QuestionRequest;
import com.app.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }


    @PostMapping("/quizzes")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody ConfigureQuiz configureQuiz) {
        QuizDTO quiz = quizService.createQuiz(configureQuiz);
        return new ResponseEntity<>(quiz, HttpStatus.CREATED);
    }

    @PostMapping("/quizzes/quiz-start")
    public ResponseEntity<QuestionDTO> startQuiz(@RequestBody QuestionRequest questionRequest) {
        QuestionDTO question = quizService.startQuiz(questionRequest.getQuizId(), questionRequest.getTopicId());
        return new ResponseEntity<>(question, HttpStatus.OK);
    }

    @PostMapping("/quizzes/quiz-questions")
    public ResponseEntity<QuestionDTO> getQuizQuestions(@RequestBody AnswerResponse answerResponse) {
        QuestionDTO question = quizService.nextQuestion(answerResponse);
        return new ResponseEntity<>(question, HttpStatus.OK);
    }

}
