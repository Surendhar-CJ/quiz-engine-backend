package com.app.quiz.repository;

import com.app.quiz.entity.Quiz;
import com.app.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByUser(User user);
}
