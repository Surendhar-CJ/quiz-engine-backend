package com.app.quiz.service.implementation;

import com.app.quiz.dto.TopicDTO;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Topic;
import com.app.quiz.repository.TopicRepository;
import com.app.quiz.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TopicServiceImplementation implements TopicService {

    private final TopicRepository topicRepository;

    @Autowired
    public TopicServiceImplementation(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public List<TopicDTO> topics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicDTO> topicDTOs = new ArrayList<>();
        // Initialize Map to store counts of different difficulty levels
        Map<String, Integer> difficultyCount = new HashMap<>();

        // Iterate over each topic
        for (Topic topic : topics) {
            // Reset counts for each new topic
            difficultyCount.put("easy", 0);
            difficultyCount.put("medium", 0);
            difficultyCount.put("hard", 0);

            // Iterate over each question for this topic
            for (Question question : topic.getQuestionsList()) {
                String difficulty = question.getDifficultyLevel().getLevel().toLowerCase();
                // Increment the count for the corresponding difficulty level
                difficultyCount.put(difficulty, difficultyCount.getOrDefault(difficulty, 0) + 1);
            }

            // Populate the DTO
            TopicDTO topicDTO = new TopicDTO(
                    topic.getId(),
                    topic.getName(),
                    topic.getQuestionsList().size(),
                    difficultyCount.get("easy"),
                    difficultyCount.get("medium"),
                    difficultyCount.get("hard")
            );

            // Add the DTO to the list
            topicDTOs.add(topicDTO);
        }

        return topicDTOs;
    }
}
