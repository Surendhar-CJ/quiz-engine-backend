package com.app.quiz.repository;

import com.app.quiz.entity.Subtopic;
import com.app.quiz.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubtopicRepository extends JpaRepository<Subtopic, Long> {
    Subtopic findByNameAndTopic(String subtopic, Topic topic);

}
