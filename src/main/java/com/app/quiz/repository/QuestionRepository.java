package com.app.quiz.repository;

import com.app.quiz.entity.Question;
import com.app.quiz.entity.Topic;
import com.app.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findQuestionsByTopic(Topic topic);
    List<Question> findByUserId(Long userId);


}
