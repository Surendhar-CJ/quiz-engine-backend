package com.app.quiz.service.implementation;

import com.app.quiz.entity.Question;
import com.app.quiz.entity.Topic;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.QuestionRepository;
import com.app.quiz.repository.TopicRepository;
import com.app.quiz.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionServiceImplementation implements QuestionService {

    private QuestionRepository questionRepository;
    private TopicRepository topicRepository;

    @Autowired
    public QuestionServiceImplementation(QuestionRepository questionRepository, TopicRepository topicRepository) {
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public List<Question> getAllQuestionsByTopic(Long topicId) {
        Optional<Topic> existingTopic = topicRepository.findById(topicId);

        Topic topic;
        if (existingTopic.isPresent()) {
            topic = existingTopic.get();
        } else {
            throw new ResourceNotFoundException("Topic not found");
        }

        return questionRepository.findQuestionsByTopic(topic);
    }


}
