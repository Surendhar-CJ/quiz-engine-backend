package com.app.quiz.utils;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.entity.Choice;
import com.app.quiz.entity.Topic;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
public final class QuizResult {
        private Long quizId;
        private Long userId;
        private Topic topic;
        private Boolean isQuizCompleted;
        private Integer noOfQuestions;
        private Integer questionsLimit;
        private String difficultyLevel;
        private Double totalNumberOfMarks;
        private Double finalScore;
        private Double finalPercentage;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private List<QuestionDTO> questions;
        private Map<Long, List<Choice>> userAnswerChoices;
        private Map<Long, List<Choice>> correctAnswerChoices;
        private Map<Long, String> answerExplanation;
}
