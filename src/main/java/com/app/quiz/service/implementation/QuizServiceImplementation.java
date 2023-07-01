package com.app.quiz.service.implementation;

import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.InvalidInputException;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.*;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.requestBody.ConfigureQuiz;
import com.app.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class QuizServiceImplementation implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;

    @Autowired
    public QuizServiceImplementation(QuizRepository quizRepository, UserRepository userRepository, TopicRepository topicRepository, QuestionRepository questionRepository, ChoiceRepository choiceRepository) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
    }

    @Override
    public Quiz createQuiz(ConfigureQuiz configureQuiz) {
        Optional<User> user = userRepository.findById(configureQuiz.getUserId());
        if(user.isEmpty()) {
            throw new ResourceNotFoundException("User with "+ configureQuiz.getUserId()+" is not found");
        }

        Optional<Topic> topic = topicRepository.findById(configureQuiz.getTopicId());
        if(topic.isEmpty()) {
            throw new ResourceNotFoundException("Topic with "+ configureQuiz.getTopicId()+" is not found");
        }

        Quiz newQuiz = new Quiz(user.get(), topic.get(), false, 0);

        return quizRepository.save(newQuiz);
    }

    @Override
    public Question startQuiz(Long quizId, Long topicId) {
        Optional<Quiz> existingQuiz = quizRepository.findById(quizId);

        Quiz quiz;
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with "+quizId+" is not found");
        } else {
            quiz = existingQuiz.get();
        }

        Optional<Topic> existingTopic = topicRepository.findById(topicId);

        Topic topic;
        if(existingTopic.isEmpty()) {
            throw new ResourceNotFoundException("Topic with "+topicId+" is not found");
        } else {
            topic = existingTopic.get();
        }

        // Pick first question
        List<Question> questions = topic.getQuestionsList();
        for(Question q : questions) {
            if(q.getDifficultyLevel().getLevel().equalsIgnoreCase("Easy")) {
                quiz.getServedQuestions().add(q);
                quizRepository.save(quiz);
                return q;
            }
        }

        throw new ResourceNotFoundException("No easy questions available");
    }

    @Override
    public Question nextQuestion(AnswerResponse answerResponse) {
        Optional<Quiz> existingQuiz = quizRepository.findById(answerResponse.getQuizId());

        Quiz quiz;
        if(existingQuiz.isEmpty()) {
            throw new ResourceNotFoundException("Quiz with "+answerResponse.getQuizId()+" is not found");
        } else {
            quiz = existingQuiz.get();
        }

        if(quiz.getIsCompleted() == true) {
            throw new IllegalArgumentException("Quiz is completed");
        }

        Optional<Question> lastQuestionOptional = questionRepository.findById(answerResponse.getQuestionId());
        if (lastQuestionOptional.isEmpty()) {
            throw new ResourceNotFoundException("Question with id "+answerResponse.getQuestionId()+" is not found");
        }
        Question lastQuestion = lastQuestionOptional.get();

        quiz.getResponses().put(lastQuestion, answerResponse.getAnswerChoices());

      //  Question nextQuestion = nextAdaptiveQuestion(quiz, answerResponse, lastQuestion);

        Question nextQuestion = nextRegularQuestion(quiz, answerResponse);

        // Add the selected question to the servedQuestions list
        quiz.getServedQuestions().add(nextQuestion);
        quiz.setIsCompleted(quiz.quizCompleted());

        quizRepository.save(quiz);


        return nextQuestion;
    }

    private String getNextDifficultyLevel(String currentDifficulty) {
        switch (currentDifficulty.toLowerCase()) {
            case "easy":
                return "medium";
            case "medium":
                return "hard";
            default:
                return "hard";
        }
    }

    private String getPreviousDifficultyLevel(String currentDifficulty) {
        switch (currentDifficulty.toLowerCase()) {
            case "hard":
                return "medium";
            case "medium":
                return "easy";
            default:
                return "easy";
        }
    }



    //For Regular quiz
    private Question nextRegularQuestion(Quiz quiz, AnswerResponse answerResponse) {
            Topic topic = quiz.getTopic();
            List<Question> servedQuestions = quiz.getServedQuestions();
            List<Question> questionsList = topic.getQuestionsList();
            Question nextQuestion = questionsList.stream()
                    .filter(question -> !servedQuestions.contains(question))
                    .findAny()
                    .orElseThrow(() -> new ResourceNotFoundException("No more questions available"));

            return nextQuestion;
    }

    private Question nextAdaptiveQuestion(Quiz quiz, AnswerResponse answerResponse, Question lastQuestion) {

        // Check if last question was answered correctly
        List<Choice> answerChoices = answerResponse.getAnswerChoices();
        List<Choice> databaseChoices = new ArrayList<>();


        // Convert the last question's choices to a set for faster lookups
        Set<Choice> lastQuestionChoices = new HashSet<>(lastQuestion.getChoices());

        for(Choice choice : answerChoices) {
            Optional<Choice> databaseChoiceOptional = choiceRepository.findById((choice.getId()));
            if(databaseChoiceOptional.isEmpty()) {
                throw new ResourceNotFoundException("Choice with id "+choice.getId()+" is not found");
            } else {
                Choice databaseChoice = databaseChoiceOptional.get();

                // Check if the choice is valid for the last question
                if (!lastQuestionChoices.contains(databaseChoice)) {
                    throw new InvalidInputException("Invalid choice id for the given question");
                }

                databaseChoices.add(databaseChoice);
            }
        }

        boolean isCorrect = true; // Assume all choices are correct
        for (Choice choice : databaseChoices) {
            if (!choice.isCorrect()) {
                isCorrect = false; // If any choice is not correct, set isCorrect to false
                break; // No need to check the rest of the choices
            }
        }

        // Select next question based on performance
        String currentDifficulty = lastQuestion.getDifficultyLevel().getLevel();
        String nextDifficulty;
        if (isCorrect) {
            nextDifficulty = getNextDifficultyLevel(currentDifficulty);
        } else {
            nextDifficulty = getPreviousDifficultyLevel(currentDifficulty);
        }

        Topic topic = quiz.getTopic();
        List<Question> allQuestions = topic.getQuestionsList();
        Question nextQuestion = allQuestions.stream()
                .filter(question -> question.getDifficultyLevel().getLevel().equalsIgnoreCase(nextDifficulty))
                .filter(question -> !quiz.getServedQuestions().contains(question)) // make sure question hasn't been served before
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No more questions available"));

        return nextQuestion;
    }

}
