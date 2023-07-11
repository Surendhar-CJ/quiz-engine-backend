package com.app.quiz.utils;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.entity.Choice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
public final class QuizResult {
        private Long quizId;
        private Long userId;
        private Boolean isQuizCompleted;
        private Integer noOfQuestions;
        private Double totalNumberOfMarks;
        private Double finalScore;
        private Double finalPercentage;
        private List<QuestionDTO> questions;
        private Map<Long, List<Choice>> userAnswerChoices;
        private Map<Long, List<Choice>> correctAnswerChoices;
        private Map<Long, String> answerExplanation;
}
