package com.app.quiz.dto.mapper;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.entity.Question;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public final class QuestionDTOMapper implements Function<Question, QuestionDTO> {

    @Override
    public QuestionDTO apply(Question question) {
        return new QuestionDTO(
                                question.getId(),
                                question.getText(),
                                question.getScore(),
                                question.getType().getType(),
                                question.getDifficultyLevel().getLevel(),
                                question.getChoices()
                            );
    }
}
