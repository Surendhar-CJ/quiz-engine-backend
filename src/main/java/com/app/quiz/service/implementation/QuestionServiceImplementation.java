package com.app.quiz.service.implementation;

import com.app.quiz.dto.QuestionDTO;
import com.app.quiz.dto.mapper.QuestionDTOMapper;
import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.DifficultyLevelRepository;
import com.app.quiz.repository.QuestionRepository;
import com.app.quiz.repository.QuestionTypeRepository;
import com.app.quiz.repository.TopicRepository;
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
    private final QuestionDTOMapper questionDTOMapper;

    @Autowired
    public QuestionServiceImplementation(QuestionRepository questionRepository, TopicRepository topicRepository, DifficultyLevelRepository difficultyLevelRepository, QuestionTypeRepository questionTypeRepository, QuestionDTOMapper questionDTOMapper) {
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.difficultyLevelRepository = difficultyLevelRepository;
        this.questionTypeRepository = questionTypeRepository;
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
        newQuestion.setText(questionAddition.getQuestionText());
        newQuestion.setScore(questionAddition.getScore());
        newQuestion.setType(questionType);
        newQuestion.setDifficultyLevel(difficultyLevel);
        newQuestion.setTopic(topic);
        newQuestion.setExplanation(questionAddition.getExplanation());

        // If question type is True or False, only two choices should be available
        if(questionType.getType().equalsIgnoreCase("True or False") && questionAddition.getChoices().size() != 2) {
            throw new IllegalArgumentException("True or False questions must have exactly two choices");
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
            throw new IllegalArgumentException("Multiple Choice Questions must have exactly one correct answer");
        }

        if(questionType.getType().equalsIgnoreCase("Multiple Answer") && correctAnswerCount < 2) {
            throw new IllegalArgumentException("Multiple Answer Questions must have at least two correct answers");
        }

        Question question = questionRepository.save(newQuestion);

        return questionDTOMapper.apply(question);
    }
}
