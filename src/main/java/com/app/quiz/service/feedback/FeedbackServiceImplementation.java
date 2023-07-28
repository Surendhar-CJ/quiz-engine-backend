package com.app.quiz.service.feedback;


import com.app.quiz.entity.*;
import com.app.quiz.exception.custom.ResourceNotFoundException;
import com.app.quiz.repository.*;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.utils.FeedbackResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public  class FeedbackServiceImplementation implements FeedbackService {
    private final FeedbackRepository feedbackRepository;

    private final FeedbackContentRepository feedbackContentRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    @Autowired
    public FeedbackServiceImplementation(FeedbackRepository feedbackRepository, FeedbackContentRepository feedbackContentRepository, UserFeedbackRepository userFeedbackRepository, UserRepository userRepository, TopicRepository topicRepository) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackContentRepository = feedbackContentRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
    }

    // This method is shared among all feedback types.
    private int countCorrectAnswers(Question question, AnswerResponse answerResponse) {
        List<Choice> answerChoices = answerResponse.getAnswerChoices();
        List<Choice> correctChoices = question.getChoices().stream().filter((choice) -> choice.isCorrect() == true).toList();
        int numberOfCorrectAnswerChoices = 0;
        for (Choice correctChoice : correctChoices) {
            for (Choice answerChoice : answerChoices) {
                if (correctChoice.getId().equals(answerChoice.getId())) {
                    numberOfCorrectAnswerChoices++;
                }
            }
        }
        return numberOfCorrectAnswerChoices;
    }

    @Override
    public FeedbackResponse generateFeedback(Quiz quiz, Question question, AnswerResponse answerResponse) {

        FeedbackResponse feedbackResponse = null;
        int numberOfCorrectAnswerChoices = countCorrectAnswers(question, answerResponse);

        String feedbackType = quiz.getFeedbackType().getType();

        if ("Immediate Response".equalsIgnoreCase(feedbackType)) {
            feedbackResponse = immediateResponse(question, numberOfCorrectAnswerChoices);
        } else if ("Immediate Correct Answer Response".equalsIgnoreCase(feedbackType)) {
            feedbackResponse = immediateCorrectAnswerResponse(question, numberOfCorrectAnswerChoices);
        } else if ("Immediate Elaborated".equalsIgnoreCase(feedbackType)) {
            feedbackResponse = immediateElaborated(question, numberOfCorrectAnswerChoices);
        }

        return feedbackResponse;
    }


    private FeedbackResponse immediateResponse(Question question, int numberOfCorrectAnswerChoices) {
        String result = "";

        List<Choice> correctChoices = question.getChoices().stream().filter(Choice::isCorrect).toList();
        int totalCorrectChoices = correctChoices.size();

        if (question.getType().getType().equalsIgnoreCase("Multiple Choice") ||
                question.getType().getType().equalsIgnoreCase("True or False")) {
            if (numberOfCorrectAnswerChoices == 0) {
                result = "Incorrect";
            } else {
                result = "Correct";
            }
        } else if(question.getType().getType().equalsIgnoreCase("Multiple Answer")) {
            if (numberOfCorrectAnswerChoices == 0) {
                result = "Incorrect";
            } else if (numberOfCorrectAnswerChoices < totalCorrectChoices) {
                result = "Partially Correct";
            } else {
                result = "Correct";
            }
        }

        FeedbackResponse feedbackResponse;

        List<Choice> correctAnswer = null;
        String explanation = "";

        feedbackResponse = new FeedbackResponse(result, correctAnswer, explanation);

        return feedbackResponse;
    }

    private FeedbackResponse immediateCorrectAnswerResponse(Question question, int numberOfCorrectAnswerChoices) {
        String result = "";
        List<Choice> correctChoices = question.getChoices().stream().filter(Choice::isCorrect).toList();
        int totalCorrectChoices = correctChoices.size();

        if (question.getType().getType().equalsIgnoreCase("Multiple Choice") ||
                question.getType().getType().equalsIgnoreCase("True or False")) {
            if (numberOfCorrectAnswerChoices == 0) {
                result = "Incorrect";
            } else {
                result = "Correct";
            }
        } else if(question.getType().getType().equalsIgnoreCase("Multiple Answer")) {
            if (numberOfCorrectAnswerChoices == 0) {
                result = "Incorrect";
            } else if (numberOfCorrectAnswerChoices < totalCorrectChoices) {
                result = "Partially Correct";
            } else {
                result = "Correct";
            }
        }

        FeedbackResponse feedbackResponse;
        List<Choice> correctAnswer = findCorrectAnswers(question);
        String explanation = "";

        feedbackResponse = new FeedbackResponse(result, correctAnswer, explanation);

        return feedbackResponse;
    }

    private FeedbackResponse immediateElaborated(Question question, int numberOfCorrectAnswerChoices) {
        String result = "";
        List<Choice> correctChoices = question.getChoices().stream().filter(Choice::isCorrect).toList();
        int totalCorrectChoices = correctChoices.size();

        if (question.getType().getType().equalsIgnoreCase("Multiple Choice") ||
                question.getType().getType().equalsIgnoreCase("True or False")) {
            if (numberOfCorrectAnswerChoices == 0) {
                result = "Incorrect";
            } else {
                result = "Correct";
            }
        } else if(question.getType().getType().equalsIgnoreCase("Multiple Answer")) {
            if (numberOfCorrectAnswerChoices == 0) {
                result = "Incorrect";
            } else if (numberOfCorrectAnswerChoices < totalCorrectChoices) {
                result = "Partially Correct";
            } else {
                result = "Correct";
            }
        }

        FeedbackResponse feedbackResponse;
        List<Choice> correctAnswer = findCorrectAnswers(question);
        String explanation = question.getExplanation();

        feedbackResponse = new FeedbackResponse(result, correctAnswer, explanation);

        return feedbackResponse;
    }


    private List<Choice> findCorrectAnswers(Question question) {
        return question.getChoices().stream()
                .filter(Choice::isCorrect)
                .collect(Collectors.toList());
    }


    @Override
    public List<Feedback> getFeedbackTypes() {
        return feedbackRepository.findAll();
    }


    @Override
    public String overallFeedback(Double percentage) {
        FeedbackContent feedback = feedbackContentRepository.findTopByMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(percentage, percentage);
        if(feedback != null) {
            return feedback.getOverallFeedback();
        }
        return "There appears to be an error with the score calculation. The score percentage falls outside of the expected range. Kindly verify the results.";
    }

    @Override
    public String subtopicFeedback(Double percentage, String subtopic) {
        FeedbackContent feedback = feedbackContentRepository.findTopByMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(percentage, percentage);
        if(feedback != null) {
            return feedback.getSubtopicFeedback().replace("{subtopic}", subtopic);
        }
        return "An error has occurred as the score percentage falls outside of the expected range. Please verify the results.";
    }


    @Override
    @Transactional
    public void addFeedback(UserFeedback userFeedback) {

        if(userFeedback.getComment() == "" || userFeedback.getComment() == null) {
            return;
        }


        if (userFeedback == null ||
                userFeedback.getFeedbackByUserId() == null ||
                userFeedback.getTopicId() == null ||
                userFeedback.getFeedbackForUserId() == null) {
            throw new IllegalArgumentException("Invalid user feedback data");
        }

        // Check if the user who is giving the feedback exists
        if (!userRepository.existsById(userFeedback.getFeedbackByUserId())) {
            throw new ResourceNotFoundException("User not found with id: " + userFeedback.getFeedbackByUserId());
        }

        // Check if the user who is receiving the feedback exists
        if (!userRepository.existsById(userFeedback.getFeedbackForUserId())) {
            throw new ResourceNotFoundException("User not found with id: " + userFeedback.getFeedbackForUserId());
        }

        // Check if the topic exists
        if (!topicRepository.existsById(userFeedback.getTopicId())) {
            throw new ResourceNotFoundException("Topic not found with id: " + userFeedback.getTopicId());
        }

        userFeedbackRepository.save(userFeedback);
    }





}
