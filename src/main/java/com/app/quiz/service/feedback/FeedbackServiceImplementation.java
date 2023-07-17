package com.app.quiz.service.feedback;


import com.app.quiz.entity.Choice;
import com.app.quiz.entity.Feedback;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.repository.FeedbackRepository;
import com.app.quiz.requestBody.AnswerResponse;
import com.app.quiz.utils.FeedbackResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public  class FeedbackServiceImplementation implements FeedbackService {
    private final FeedbackConfiguration feedbackConfig;
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackServiceImplementation(FeedbackConfiguration feedbackConfig, FeedbackRepository feedbackRepository) {
        this.feedbackConfig = feedbackConfig;
        this.feedbackRepository = feedbackRepository;
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

        if ("Immediate_Response".equalsIgnoreCase(feedbackType)) {
            feedbackResponse = immediateResponse(question, numberOfCorrectAnswerChoices);
        } else if ("Immediate_Correct_Answer_Response".equalsIgnoreCase(feedbackType)) {
            feedbackResponse = immediateCorrectAnswerResponse(question, numberOfCorrectAnswerChoices);
        } else if ("Immediate_Elaborated".equalsIgnoreCase(feedbackType)) {
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

}
