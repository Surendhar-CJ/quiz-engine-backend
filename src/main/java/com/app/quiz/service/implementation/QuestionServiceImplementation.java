package com.app.quiz.service.implementation;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.dto.mapper.QuestionDTOMapper;
import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.InvalidInputException;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.*;
import com.app.quiz.requestBody.QuestionAddition;
import com.app.quiz.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuestionServiceImplementation implements QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final QuestionTypeRepository questionTypeRepository;
    private final SubtopicRepository subtopicRepository;
    private final QuestionDTOMapper questionDTOMapper;

    @Autowired
    public QuestionServiceImplementation(QuestionRepository questionRepository, TopicRepository topicRepository, DifficultyLevelRepository difficultyLevelRepository, SubtopicRepository subtopicRepository, QuestionTypeRepository questionTypeRepository, QuestionDTOMapper questionDTOMapper) {
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.difficultyLevelRepository = difficultyLevelRepository;
        this.questionTypeRepository = questionTypeRepository;
        this.subtopicRepository = subtopicRepository;
        this.questionDTOMapper =  questionDTOMapper;
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


    @Override
    public QuestionDTO addQuestion(QuestionAddition questionAddition) {

        if(questionAddition.getTopicId() == null) {
            throw new InvalidInputException("Topic id cannot be null");
        }

        Optional<Topic> existingTopic = topicRepository.findById(questionAddition.getTopicId());

        Topic topic;
        if (existingTopic.isPresent()) {
            topic = existingTopic.get();
        } else {
            throw new ResourceNotFoundException("Topic not found");
        }

        Optional<DifficultyLevel> existingDifficultyLevel = difficultyLevelRepository.findByLevel(questionAddition.getDifficultyLevel().toUpperCase());

        DifficultyLevel difficultyLevel;
        if(existingDifficultyLevel.isEmpty()) {
            throw new ResourceNotFoundException("Difficult level "+questionAddition.getDifficultyLevel()+ " is not found");
        } else {
            difficultyLevel = existingDifficultyLevel.get();
        }

        Optional<QuestionType> existingQuestionType = questionTypeRepository.findByType(questionAddition.getQuestionType());

        QuestionType questionType;
        if(existingQuestionType.isEmpty()) {
            throw new ResourceNotFoundException("Question type "+questionAddition.getQuestionType()+" is not found");
        } else {
            questionType = existingQuestionType.get();
        }

        Question newQuestion = new Question();

        newQuestion.setTopic(topic);

        if(questionAddition.getQuestionText() == null || questionAddition.getQuestionText().equals("")) {
            throw new InvalidInputException("Question cannot be empty");
        }
        newQuestion.setText(questionAddition.getQuestionText());


        Double score = questionAddition.getScore();
        if(score == null) {
            switch (difficultyLevel.getLevel().toLowerCase()) {
                case "easy":
                    newQuestion.setScore(1.0);
                    break;
                case "medium":
                    newQuestion.setScore(2.0);
                    break;
                case "hard":
                    newQuestion.setScore(3.0);
                    break;
            }
        } else {
            newQuestion.setScore(score);
        }


        newQuestion.setType(questionType);

        newQuestion.setDifficultyLevel(difficultyLevel);

        if (questionAddition.getSubtopic() == null || questionAddition.getSubtopic().equals("")) {
            Subtopic subtopic = subtopicRepository.findByNameAndTopic("General", topic);

            // If a "General" Subtopic doesn't exist for the given Topic, create one.
            if (subtopic == null) {
                subtopic = new Subtopic();
                subtopic.setName("General");
                subtopic.setTopic(topic);
                subtopic = subtopicRepository.save(subtopic);
            }

            newQuestion.setSubtopic(subtopic);
        }


        if(questionAddition.getExplanation().equals("") || questionAddition.getExplanation() == null) {
            throw new InvalidInputException("Answer explanation cannot be empty");
        }
        newQuestion.setExplanation(questionAddition.getExplanation());

        // If question type is True or False, only two choices should be available
        if(questionType.getType().equalsIgnoreCase("True or False") && questionAddition.getChoices().size() != 2) {
            throw new InvalidInputException("True or False questions must have exactly two choices");
        }

        int correctAnswerCount = 0;
        for(Map.Entry<String, Boolean> entry : questionAddition.getChoices().entrySet()) {
            String currentChoice = entry.getKey();
            boolean isCorrect = entry.getValue();
            if (isCorrect) correctAnswerCount++;
            Choice choice = new Choice(currentChoice, isCorrect);
            choice.setQuestion(newQuestion);
            newQuestion.getChoices().add(choice);
        }

        if(questionType.getType().equalsIgnoreCase("Multiple Choice") && correctAnswerCount != 1) {
            throw new InvalidInputException("Multiple Choice Questions must have exactly one correct answer");
        }

        if(questionType.getType().equalsIgnoreCase("Multiple Answer") && correctAnswerCount < 2) {
            throw new InvalidInputException("Multiple Answer Questions must have at least two correct answers");
        }

        Question question = questionRepository.save(newQuestion);

        return questionDTOMapper.apply(question);
    }
}
