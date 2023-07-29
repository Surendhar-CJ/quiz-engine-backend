package com.app.quiz.service;

import com.app.quiz.dto.TopicDTO;
import com.app.quiz.entity.Rating;
import com.app.quiz.requestBody.TopicCreation;

import java.util.List;

public interface TopicService {

    List<TopicDTO> topics();

    List<TopicDTO> createTopic(TopicCreation topicCreation);

    TopicDTO getTopic(String name);

    void deleteTopicById(Long topicId, Long userId);

    Double rateTopic(Rating rating);

}
