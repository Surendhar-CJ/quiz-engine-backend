package com.app.quiz.service;

import com.app.quiz.dto.TopicDTO;
import com.app.quiz.entity.Topic;

import java.util.List;

public interface TopicService {

    List<TopicDTO> topics();
}
