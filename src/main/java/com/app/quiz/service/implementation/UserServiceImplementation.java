package com.app.quiz.service.implementation;

import com.app.quiz.dto.QuizDTO;
import com.app.quiz.dto.UserDTO;
import com.app.quiz.dto.UserQuizDTO;
import com.app.quiz.dto.mapper.QuizDTOMapper;
import com.app.quiz.dto.mapper.UserDTOMapper;
import com.app.quiz.entity.Quiz;
import com.app.quiz.entity.Topic;
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
import com.app.quiz.utils.RegexPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final TopicRepository topicRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDTOMapper userDTOMapper;
    private final QuizDTOMapper quizDTOMapper;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, QuizRepository quizRepository, TopicRepository topicRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UserDTOMapper userDTOMapper, QuizDTOMapper quizDTOMapper) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.topicRepository = topicRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDTOMapper = userDTOMapper;
        this.quizDTOMapper = quizDTOMapper;
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

        List<QuizDTO> userQuizzesDTO = user.getQuizList().stream()
                .filter(quiz -> quiz.getIsCompleted())
                .map(quiz -> quizDTOMapper.apply(quiz))
                .toList();
        Map<Long, Double> averageScoreByTopic = averageScoreByTopic(user);

        UserQuizDTO userQuizDTO = new UserQuizDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                userQuizzesDTO,
                averageScoreByTopic
        );
        return userQuizDTO;
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

    private Map<Long, Double> averageScoreByTopic(User user) {
        List<Quiz> quizList = user.getQuizList();
        Map<Long, Double> sumScoreByTopic = new HashMap<>();
        Map<Long, Integer> countByTopic = new HashMap<>();

        for(Quiz quiz : quizList) {
            if(!quiz.getIsCompleted()) {
                continue;
            }

            else {
                Long topicId = quiz.getTopic().getId();
                Double finalScore = quiz.getFinalScore();

                // Handle sum of scores
                if (sumScoreByTopic.containsKey(topicId)) {
                    sumScoreByTopic.put(topicId, sumScoreByTopic.get(topicId) + finalScore);
                } else {
                    sumScoreByTopic.put(topicId, finalScore);
                }

                // Handle count of quizzes
                if (countByTopic.containsKey(topicId)) {
                    countByTopic.put(topicId, countByTopic.get(topicId) + 1);
                } else {
                    countByTopic.put(topicId, 1);
                }
            }
        }

        Map<Long, Double> averageScoreByTopic = new HashMap<>();
        for (Long topicId : sumScoreByTopic.keySet()) {
            averageScoreByTopic.put(topicId, sumScoreByTopic.get(topicId) / countByTopic.get(topicId));
        }

        return averageScoreByTopic;
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
