package com.app.quiz.repository;

import com.app.quiz.entity.FeedbackContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackContentRepository extends JpaRepository<FeedbackContent, Long> {

    @Query("SELECT feedback FROM FeedbackContent feedback" +
            " WHERE feedback.minScore <= ?1 AND feedback.maxScore >= ?2")
    FeedbackContent findByScoreRange(Double minScore, Double maxScore);
}
