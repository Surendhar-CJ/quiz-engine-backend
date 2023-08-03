package com.app.quiz.repository;

import com.app.quiz.entity.Question;
import com.app.quiz.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.topic = :topic AND q.isDeleted = false")
    List<Question> findQuestionsByTopic(Topic topic);


    @Query("SELECT q FROM Question q WHERE q.user.id = :userId AND q.isDeleted = false")
    List<Question> findByUserId(Long userId);

}
