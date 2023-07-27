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
        private String feedbackType;
        private Boolean isQuizCompleted;
        private Integer noOfQuestions;
        private Integer questionsLimit;
        private String difficultyLevel;
        private Double totalNumberOfMarks;
        private Double finalScore;
        private Double finalPercentage;
        private String overallFeedback;
        private String feedbackBySubtopic;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private List<QuestionDTO> questions;
        private Map<Long, List<Choice>> userAnswerChoices;
        private Map<Long, List<Choice>> correctAnswerChoices;
        private Map<Long, String> answerExplanation;
        private Map<String, Double> marksScoredPerTopic;
        private Map<String, Double> totalMarksPerTopic;
        private Map<String, Double> percentagePerTopic;
        private Boolean isRated;
}
