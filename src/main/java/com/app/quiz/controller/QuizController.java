package com.app.quiz.controller;


import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.dto.QuizDTO;
import com.app.quiz.entity.Feedback;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.requestBody.ConfigureQuiz;
import com.app.quiz.requestBody.QuestionRequest;
import com.app.quiz.service.QuizService;
import com.app.quiz.service.feedback.FeedbackService;
import com.app.quiz.utils.FeedbackResponse;
import com.app.quiz.utils.QuizResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class QuizController {

    private final QuizService quizService;
    private final FeedbackService feedbackService;

    @Autowired
    public QuizController(QuizService quizService, FeedbackService feedbackService) {
        this.quizService = quizService;
        this.feedbackService = feedbackService;
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
    public ResponseEntity<QuestionDTO> getQuizQuestion(@RequestBody AnswerResponse answerResponse) {
        QuestionDTO question = quizService.nextQuestion(answerResponse);
        return new ResponseEntity<>(question, HttpStatus.OK);
    }

    @PostMapping("/quizzes/submit-answer")
    public ResponseEntity<FeedbackResponse> getFeedback(@RequestBody AnswerResponse answerResponse) {
            FeedbackResponse feedbackResponse = quizService.getFeedback(answerResponse);
            return new ResponseEntity<>(feedbackResponse, HttpStatus.OK);
    }

    @GetMapping("/quizzes/quiz-finish/{quizId}")
    public ResponseEntity<QuizResult> finishQuiz(@PathVariable("quizId") Long quizId) {
        QuizResult quizResult = quizService.finishQuiz(quizId);
        return new ResponseEntity<>(quizResult, HttpStatus.OK);
    }


    @GetMapping("/quiz-configuration")
    public ResponseEntity<List<Feedback>> getFeedbackTypes() {
        return new ResponseEntity<>(feedbackService.getFeedbackTypes(), HttpStatus.OK);
    }


}
