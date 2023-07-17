package com.app.quiz.service.implementation;

import com.app.quiz.dto.UserDTO;
import com.app.quiz.dto.UserQuizDTO;
import com.app.quiz.dto.mapper.QuizDTOMapper;
import com.app.quiz.dto.mapper.UserDTOMapper;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.entity.User;
import com.app.quiz.exception.custom.InvalidCredentialsException;
import com.app.quiz.exception.custom.InvalidInputException;
import com.app.quiz.exception.custom.ResourceExistsException;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.QuizRepository;
import com.app.quiz.repository.TopicRepository;
import com.app.quiz.repository.UserRepository;
import com.app.quiz.requestBody.UserLogin;
import com.app.quiz.service.UserService;
import com.app.quiz.utils.QuizResult;
import com.app.quiz.utils.RegexPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDTOMapper userDTOMapper;
    private final QuizServiceImplementation quizServiceImplementation;
    @Autowired
    public UserServiceImplementation(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UserDTOMapper userDTOMapper, QuizServiceImplementation quizServiceImplementation) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDTOMapper = userDTOMapper;
        this.quizServiceImplementation = quizServiceImplementation;

    }

    @Override
    public UserDTO createUser(User user) {
        User validatedUser = validateUser(user);

        validatedUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(validatedUser);

        return userDTOMapper.apply(validatedUser);
    }

    @Override
    public UserQuizDTO getUserById(Long id) {
        Optional<User> existingUser = userRepository.findById(id);

        User user;
        if(existingUser.isPresent()) {
            user = existingUser.get();
        }
        else {
            throw new ResourceNotFoundException("User with userId - "+id+" not found");
        }

        List<QuizResult> userQuizzes = user.getQuizList().stream()
                .filter(quiz -> quiz.getIsCompleted())
                .map(quiz -> quizServiceImplementation.getQuizResult(quiz.getId()))
                .toList();

        Map<Long, Double> averageScoreByTopic = averagePercentageByTopic(user);

        return new UserQuizDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                userQuizzes,
                averageScoreByTopic
        );
    }

    @Override
    public UserDTO login(UserLogin userLogin) {

        final String email = userLogin.getUsername();
        final String password = userLogin.getPassword();

        User user = userRepository.findUserByEmail(email);

        if(user == null || !user.getEmail().matches(email)) {
            throw new ResourceNotFoundException("User does not exist, please check your email");
        }
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return userDTOMapper.apply(user);
    }

    private Map<Long, Double> averagePercentageByTopic(User user) {
        List<Quiz> quizList = user.getQuizList();
        Map<Long, Double> sumPercentageByTopic = new HashMap<>();
        Map<Long, Integer> countByTopic = new HashMap<>();

        DecimalFormat df = new DecimalFormat("#.##");

        for (Quiz quiz : quizList) {
            if (!quiz.getIsCompleted()) {
                continue;
            } else {
                Long topicId = quiz.getTopic().getId();

                // Calculate the percentage for the current quiz
                Double totalScore = quiz.getServedQuestions().stream()
                        .mapToDouble(Question::getScore)
                        .sum();
                Double finalScore = quiz.getFinalScore();
                double quizPercentage = ((double) finalScore / totalScore) * 100;
                quizPercentage = Double.valueOf(df.format(quizPercentage));

                // Update the sum and count for the current topic
                sumPercentageByTopic.put(topicId, sumPercentageByTopic.getOrDefault(topicId, 0.0) + quizPercentage);
                countByTopic.put(topicId, countByTopic.getOrDefault(topicId, 0) + 1);
            }
        }

        // Calculate the average percentage for each topic
        Map<Long, Double> averagePercentageByTopic = new HashMap<>();
        for (Long topicId : sumPercentageByTopic.keySet()) {
            double averagePercentage = sumPercentageByTopic.get(topicId) / countByTopic.get(topicId);
            averagePercentageByTopic.put(topicId, Double.valueOf(df.format(averagePercentage)));
        }

        return averagePercentageByTopic;
    }



    private User validateUser(User user) {

        if(user.getFirstName().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty");
        }

        if(user.getLastName().isEmpty()) {
            throw new InvalidInputException("Lastname cannot be empty");
        }

        if(userRepository.findUserByEmail(user.getEmail()) != null) {
            throw new ResourceExistsException("User account with the email exists already");
        }

        if(!user.getEmail().matches(RegexPattern.EMAIL_PATTERN)) {
            throw new InvalidInputException("Invalid email address");
        }

        if(user.getPassword() == null || user.getPassword().matches("(?i)null")) {
            throw new InvalidInputException("Invalid Password, please try again");
        }

        if (!user.getPassword().matches(RegexPattern.PASSWORD_PATTERN)) {
            throw new InvalidInputException("Password should be at least 8 characters (with at least a number and a symbol from !@#$%^&*)");
        }

        return user;
    }
}
