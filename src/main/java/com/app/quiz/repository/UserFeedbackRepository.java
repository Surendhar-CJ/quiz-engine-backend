package com.app.quiz.repository;

import com.app.quiz.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    List<UserFeedback> findByFeedbackForUserId(@Param("userId") Long userId);

}
