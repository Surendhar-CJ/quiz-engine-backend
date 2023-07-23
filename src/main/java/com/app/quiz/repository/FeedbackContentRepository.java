package com.app.quiz.repository;

import com.app.quiz.entity.FeedbackContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackContentRepository extends JpaRepository<FeedbackContent, Long> {
    FeedbackContent findTopByMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(Double minScore, Double maxScore);
}
