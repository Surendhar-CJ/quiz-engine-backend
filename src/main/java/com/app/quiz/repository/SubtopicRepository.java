package com.app.quiz.repository;

import com.app.quiz.entity.Subtopic;
import com.app.quiz.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SubtopicRepository extends JpaRepository<Subtopic, Long> {
    @Query("SELECT s FROM Subtopic s WHERE lower(s.name) = lower(:name) and s.topic = :topic")
    Subtopic findByNameAndTopic(@Param("name") String name, @Param("topic") Topic topic);

    @Query("SELECT s FROM Subtopic s WHERE s.topic.id = :topicId")
    List<Subtopic> findByTopicId(@Param("topicId") Long topicId);

}
