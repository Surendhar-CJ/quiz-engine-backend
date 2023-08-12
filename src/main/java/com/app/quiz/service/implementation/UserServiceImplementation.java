package com.app.quiz.service.implementation;

import com.app.quiz.dto.*;
import com.app.quiz.dto.mapper.UserDTOMapper;
import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.InvalidCredentialsException;
import com.app.quiz.exception.custom.InvalidInputException;
import com.app.quiz.exception.custom.ResourceExistsException;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.*;
import com.app.quiz.requestBody.UserLogin;
import com.app.quiz.requestBody.UserSignUp;
import com.app.quiz.service.UserService;
import com.app.quiz.utils.QuizResult;
import com.app.quiz.utils.RegexPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDTOMapper userDTOMapper;
    private final QuizRepository quizRepository;
    private final TopicRepository topicRepository;
    private final RatingRepository ratingRepository;
    private final QuestionRepository questionRepository;
    private final QuizServiceImplementation quizServiceImplementation;
    private final UserFeedbackRepository userFeedbackRepository;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UserDTOMapper userDTOMapper, QuizRepository quizRepository, TopicRepository topicRepository, RatingRepository ratingRepository, QuestionRepository questionRepository, QuizServiceImplementation quizServiceImplementation, UserFeedbackRepository userFeedbackRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDTOMapper = userDTOMapper;
        this.quizRepository = quizRepository;
        this.topicRepository = topicRepository;
        this.ratingRepository = ratingRepository;
        this.questionRepository = questionRepository;
        this.quizServiceImplementation = quizServiceImplementation;
        this.userFeedbackRepository = userFeedbackRepository;
    }

    @Override
    public UserDTO createUser(UserSignUp userSignUp) {
        User validatedUser = validateUser(userSignUp);

        validatedUser.setPassword(bCryptPasswordEncoder.encode(validatedUser.getPassword()));
        userRepository.save(validatedUser);

        return userDTOMapper.apply(validatedUser);
    }



    @Override
    public UserProfileDTO getUserById(Long id) {
        User user = findUserById(id);

        List<QuizResult> userQuizzes = getUserQuizzes(user);
        List<UserTopicDTO> topicsCreated = getUserTopics(user);
        List<UserQuestionDTO> questionsCreated = getUserQuestions(user);
        List<UserFeedbackDTO> feedbacksReceived = getUserFeedbacks(user);

        Map<Long, Double> averageScoreByTopic = averagePercentageByTopic(user);
        Map<Long, Double> averageScoreByOtherUsersPerTopic = averagePercentageByOtherUsersPerTopic(user.getId());

        return new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                userQuizzes,
                topicsCreated,
                questionsCreated,
                feedbacksReceived,
                averageScoreByTopic,
                averageScoreByOtherUsersPerTopic
        );
    }

    private User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with userId - "+id+" not found"));
    }

    private List<QuizResult> getUserQuizzes(User user) {
        return user.getQuizList().stream()
                .filter(quiz -> quiz.getIsCompleted() && quiz.getCompletedAt() != null)
                .map(quiz -> quizServiceImplementation.getQuizResult(quiz.getId()))
                .toList();
    }

    private List<UserTopicDTO> getUserTopics(User user) {
        return topicRepository.findAllByUser(user).stream().map(topic -> {
            Integer numberOfUsersRated = ratingRepository.countByTopicId(topic.getId());
            return new UserTopicDTO(
                    topic.getId(),
                    topic.getName(),
                    Double.parseDouble(String.format("%.1f", topic.getRating())),
                    numberOfUsersRated
            );
        }).toList();
    }

    private List<UserQuestionDTO> getUserQuestions(User user) {
        Long userId = user.getId();
        return questionRepository.findByUserId(userId).stream()
                .filter(question -> !question.getIsDeleted())
                .map(question -> new UserQuestionDTO(
                        question.getId(),
                        question.getTopic().getName(),
                        question.getText(),
                        question.getChoices(),
                        question.getExplanation()))
                .collect(Collectors.toList());
    }

    private List<UserFeedbackDTO> getUserFeedbacks(User user) {
        Long userId = user.getId();
        return userFeedbackRepository.findByFeedbackForUserId(userId).stream().map(feedback -> {
            User feedbackByUser = userRepository.findById(feedback.getFeedbackByUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Topic feedbackTopic = topicRepository.findById(feedback.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            return new UserFeedbackDTO(
                    feedbackByUser.getFirstName() + " " + feedbackByUser.getLastName(),
                    feedbackTopic.getName(),
                    feedback.getComment()
            );
        }).collect(Collectors.toList());
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
        return calculateAveragePercentage(user.getQuizList());
    }

    private Map<Long, Double> averagePercentageByOtherUsersPerTopic(Long currentUserId) {
        List<Quiz> allQuizzes = quizRepository.findByUserIdNot(currentUserId);
        return calculateAveragePercentage(allQuizzes);
    }

    private Map<Long, Double> calculateAveragePercentage(List<Quiz> quizzes) {
        Map<Long, Double> sumPercentageByTopic = new HashMap<>();
        Map<Long, Integer> countByTopic = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.##");

        for (Quiz quiz : quizzes) {
            if (isQuizValid(quiz)) {
                Long topicId = quiz.getTopic().getId();
                double quizPercentage = computeQuizPercentage(quiz, df);

                sumPercentageByTopic.put(topicId, sumPercentageByTopic.getOrDefault(topicId, 0.0) + quizPercentage);
                countByTopic.put(topicId, countByTopic.getOrDefault(topicId, 0) + 1);
            }
        }

        return computeAveragePercentageByTopic(sumPercentageByTopic, countByTopic, df);
    }

    private boolean isQuizValid(Quiz quiz) {
        return quiz.getIsCompleted() && quiz.getCompletedAt() != null;
    }

    private double computeQuizPercentage(Quiz quiz, DecimalFormat df) {
        Double totalScore = quiz.getServedQuestions().stream()
                .mapToDouble(Question::getScore)
                .sum();
        Double finalScore = quiz.getFinalScore();
        double quizPercentage = ((double) finalScore / totalScore) * 100;
        return Double.valueOf(df.format(quizPercentage));
    }

    private Map<Long, Double> computeAveragePercentageByTopic(Map<Long, Double> sumPercentageByTopic,
                                                              Map<Long, Integer> countByTopic,
                                                              DecimalFormat df) {
        Map<Long, Double> averagePercentageByTopic = new HashMap<>();
        for (Long topicId : sumPercentageByTopic.keySet()) {
            double averagePercentage = sumPercentageByTopic.get(topicId) / countByTopic.get(topicId);
            averagePercentageByTopic.put(topicId, Double.valueOf(df.format(averagePercentage)));
        }
        return averagePercentageByTopic;
    }



    private User validateUser(UserSignUp userSignUp) {

        User user = new User(userSignUp.getFirstName(), userSignUp.getLastName(), userSignUp.getEmail(), userSignUp.getPassword());

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
            throw new InvalidInputException("Password - min. 8 characters, a number & a symbol ");
        }

        if(!user.getPassword().matches(userSignUp.getConfirmPassword())) {
            throw new InvalidInputException("Passwords do not match");
        }

        return user;
    }


}
