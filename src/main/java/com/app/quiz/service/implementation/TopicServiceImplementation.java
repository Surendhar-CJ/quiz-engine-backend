package com.app.quiz.service.implementation;

import com.app.quiz.dto.TopicDTO;
import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.InvalidInputException;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.RatingRepository;
import com.app.quiz.repository.SubtopicRepository;
import com.app.quiz.repository.TopicRepository;
import com.app.quiz.repository.UserRepository;
import com.app.quiz.requestBody.TopicCreation;
import com.app.quiz.service.TopicService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TopicServiceImplementation implements TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final SubtopicRepository subtopicRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public TopicServiceImplementation(TopicRepository topicRepository, UserRepository userRepository, RatingRepository ratingRepository, SubtopicRepository subtopicRepository) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.subtopicRepository = subtopicRepository;
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

            Integer numberOfUsersRated = ratingRepository.countByTopicId(topic.getId());
            List<Subtopic> subtopics = subtopicRepository.findByTopicId(topic.getId());

            // Populate the DTO
            TopicDTO topicDTO = new TopicDTO(
                    topic.getId(),
                    topic.getName(),
                    topic.getUser().getFirstName()+" "+topic.getUser().getLastName(),
                    Double.parseDouble(String.format("%.1f", topic.getRating())), // Format the rating,
                    numberOfUsersRated,
                    topic.getQuestionsList().size(),
                    difficultyCount.get("easy"),
                    difficultyCount.get("medium"),
                    difficultyCount.get("hard"),
                    subtopics
            );

            // Add the DTO to the list
            topicDTOs.add(topicDTO);
        }

        return topicDTOs;
    }



    @Override
    public List<TopicDTO> createTopic(TopicCreation topicCreation) {

        String name = topicCreation.getTopicName();
        Long userId = topicCreation.getUserId();

        Optional<User> user = userRepository.findById(userId);

        if(user.isEmpty()) {
            throw new InvalidInputException("User id not found");
        }
        // Convert first letter to uppercase and rest to lowercase
        String formattedName = formatName(name);

        Topic topic = new Topic(formattedName, user.get());

        topicRepository.save(topic);

        return topics();

    }




    @Override
    @Transactional
    public void deleteTopicById(Long topicId, Long userId) {
        Optional<Topic> topicOptional = topicRepository.findById(topicId);

        if (topicOptional.isEmpty()) {
            throw new ResourceNotFoundException("Topic not found");
        }

        Topic topic = topicOptional.get();

        // Assuming that the Topic entity has a getUser method that returns the User who created the topic
        User creator = topic.getUser();

        if (creator == null || !creator.getId().equals(userId)) {
            throw new InvalidInputException("Only the creator can delete this topic");
        }

        // Delete ratings associated with the topic
        ratingRepository.deleteByTopicId(topicId);

        topicRepository.delete(topic);
    }





    @Override
    public Double rateTopic(Rating rating) {
        if(rating.getRating() < 0.5 || rating.getRating() > 5) {
            throw new InvalidInputException("Rating should be between 0.5 and 5");
        }

        // Fetch the topic
        Optional<Topic> topicOptional = topicRepository.findById(rating.getTopicId());
        if (!topicOptional.isPresent()) {
            throw new ResourceNotFoundException("Topic not found with id: " + rating.getTopicId());
        }

        Topic topic = topicOptional.get();

        // Check if the user who is rating is the same user who created the topic
        if (rating.getUserId().equals(topic.getUser().getId())) {
            throw new InvalidInputException("User cannot rate their own topic");
        }

        // Check if the user has already rated this topic
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndTopicId(rating.getUserId(), rating.getTopicId());
        if (existingRating.isPresent()) {
            throw new InvalidInputException("User has already rated this topic");
        }

        // Save the new rating
        ratingRepository.save(rating);

        // Calculate the new average rating for the topic
        Double averageRating = ratingRepository.findAverageRatingByTopicId(rating.getTopicId());

        // Set the new average rating
        topic.setRating(averageRating);

        // Save the updated topic
        topicRepository.save(topic);

        return averageRating;
    }



    private String formatName(String name) {
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            String formattedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            sb.append(formattedWord).append(" ");
        }

        return sb.toString().trim();
    }


}
