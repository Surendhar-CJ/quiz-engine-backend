package com.app.quiz.service.implementation;

import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.QuestionRepository;
import com.app.quiz.repository.QuizRepository;
import com.app.quiz.repository.TopicRepository;
import com.app.quiz.repository.UserRepository;
import com.app.quiz.requestBody.StartQuiz;
import com.app.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class QuizServiceImplementation implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuizServiceImplementation(QuizRepository quizRepository, UserRepository userRepository, TopicRepository topicRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public Quiz createQuiz(StartQuiz startQuiz) {
        Optional<User> user = userRepository.findById(startQuiz.getUserId());
        if(user.isEmpty()) {
            throw new ResourceNotFoundException("User with "+startQuiz.getUserId()+" is not found");
        }

        Optional<Topic> topic = topicRepository.findById(startQuiz.getTopicId());
        if(topic.isEmpty()) {
            throw new ResourceNotFoundException("Topic with "+startQuiz.getTopicId()+" is not found");
        }

        Quiz newQuiz = new Quiz(user.get(), topic.get(), false, 0);

        return quizRepository.save(newQuiz);
    }

    @Override
    public Question questionGenerator(Long quizId, Long topicId) {
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
        }
        else {
            topic = existingTopic.get();
        }

        Question question = null;
        //First Question
        if(quiz.getServedQuestions().size() == 0) {
            List<Question> questions = topic.getQuestionsList();
            for(Question q : questions) {
                if(q.getDifficultyLevel().getLevel().equalsIgnoreCase("Easy")) {
                    question = q;
                    quiz.getServedQuestions().add(q);
                }
            }
            return question;
        } else {
            // Add logic here to select the next question based on user's performance
            // Get last served question
            Question lastQuestion = quiz.getServedQuestions().get(quiz.getServedQuestions().size() - 1);

            // Check if last question was answered correctly
            List<Choice> userResponse = quiz.getResponses().get(lastQuestion);

            // Assume all choices for a question are stored in userResponse
            boolean isCorrect = userResponse.stream().allMatch(Choice::isCorrect);

            // Select next question based on performance
            String currentDifficulty = lastQuestion.getDifficultyLevel().getLevel();
            String nextDifficulty;

            if (isCorrect) {
                // if last question was answered correctly, increase the difficulty if possible
                if (currentDifficulty.equalsIgnoreCase("Easy")) {
                    nextDifficulty = "Medium";
                } else if (currentDifficulty.equalsIgnoreCase("Medium")) {
                    nextDifficulty = "Hard";
                } else {
                    nextDifficulty = "Hard"; // keep it "Hard" if it's already "Hard"
                }
            } else {
                // if last question was answered incorrectly, keep the difficulty same or decrease
                if (currentDifficulty.equalsIgnoreCase("Hard")) {
                    nextDifficulty = "Medium";
                } else if (currentDifficulty.equalsIgnoreCase("Medium")) {
                    nextDifficulty = "Easy";
                } else {
                    nextDifficulty = "Easy"; // keep it "Easy" if it's already "Easy"
                }
            }

            // Get the question of next difficulty
            List<Question> allQuestions = topic.getQuestionsList();
            Question nextQuestion = allQuestions.stream()
                    .filter(q -> q.getDifficultyLevel().getLevel().equalsIgnoreCase(nextDifficulty))
                    .filter(q -> !quiz.getServedQuestions().contains(q)) // make sure question hasn't been served before
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No more questions available"));

            // Add the selected question to the servedQuestions list
            quiz.getServedQuestions().add(nextQuestion);
            return nextQuestion;
        }

    }

}
