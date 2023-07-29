package com.app.quiz.repository;

import com.app.quiz.entity.Topic;
import com.app.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findAllByUser(User user);

    Optional<Topic> findByNameIgnoreCase(String name);
}
