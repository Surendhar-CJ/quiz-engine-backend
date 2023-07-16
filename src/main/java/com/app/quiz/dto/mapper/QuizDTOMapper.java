package com.app.quiz.dto.mapper;

import com.app.quiz.dto.QuizDTO;
import com.app.quiz.entity.Quiz;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public final class QuizDTOMapper implements Function<Quiz, QuizDTO> {

    @Override
    public QuizDTO apply(Quiz quiz) {
        return new QuizDTO(quiz.getId(),
                           quiz.getUser().getId(),
                           quiz.getTopic(),
                           quiz.getFeedbackType(),
                           quiz.getIsCompleted(),
                           quiz.getQuestionsLimit(),
                           quiz.getDifficultyLevel() != null ? quiz.getDifficultyLevel().getLevel() : null,
                           quiz.getFinalScore(),
                           quiz.getCreatedAt(),
                           quiz.getServedQuestions(),
                           quiz.getResponses());
    }
}
