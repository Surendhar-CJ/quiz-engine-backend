package com.app.quiz.service.implementation;

import com.app.quiz.entity.Topic;
import com.app.quiz.repository.TopicRepository;
import com.app.quiz.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImplementation implements TopicService {

    private final TopicRepository topicRepository;

    @Autowired
    public TopicServiceImplementation(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public List<Topic> topics() {
        List<Topic> topics =  topicRepository.findAll();
        for(Topic topic : topics) {
            topic.setNumberOfQuestions(topic.getQuestionsList().size());
        }
        return topics;
    }
}
