package com.app.quiz.repository;

import com.app.quiz.entity.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DifficultyLevelRepository extends JpaRepository<DifficultyLevel, Long> {
    Optional<DifficultyLevel> findByLevel(String level);
}
