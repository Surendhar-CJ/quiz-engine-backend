package com.app.quiz.repository;

import com.app.quiz.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.topicId = :topicId")
    Double findAverageRatingByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT r FROM Rating r WHERE r.userId = :userId AND r.topicId = :topicId")
    Optional<Rating> findByUserIdAndTopicId(@Param("userId") Long userId, @Param("topicId") Long topicId);

    Integer countByTopicId(Long id);

    void deleteByTopicId(Long topicId);

}
