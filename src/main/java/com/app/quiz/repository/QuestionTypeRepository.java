package com.app.quiz.repository;

import com.app.quiz.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuestionTypeRepository extends JpaRepository<QuestionType, Long> {
    Optional<QuestionType> findByType(String type);
}
