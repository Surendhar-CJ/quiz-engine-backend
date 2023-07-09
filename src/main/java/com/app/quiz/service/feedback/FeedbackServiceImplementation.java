package com.app.quiz.service.feedback;


import com.app.quiz.entity.Choice;
import com.app.quiz.entity.Feedback;
import com.app.quiz.entity.Question;
import com.app.quiz.entity.Quiz;
import com.app.quiz.repository.FeedbackRepository;
import com.app.quiz.requestBody.AnswerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public String generateFeedback(Quiz quiz, Question question, AnswerResponse answerResponse) {
        int numberOfCorrectAnswerChoices = countCorrectAnswers(question, answerResponse);

        String feedbackType = quiz.getFeedbackType().getType();
        String feedback ="";

        if ("Immediate_Response".equalsIgnoreCase(feedbackType)) {
            feedback = immediateResponse(question, numberOfCorrectAnswerChoices);
        } else if ("Immediate_Correct_Answer_Response".equalsIgnoreCase(feedbackType)) {
            feedback = immediateCorrectAnswerResponse(question, numberOfCorrectAnswerChoices);
        } else if ("Immediate_Elaborated".equalsIgnoreCase(feedbackType)) {
            feedback = immediateElaborated(question, numberOfCorrectAnswerChoices);
        }

        return feedback;
    }


    private String immediateResponse(Question question, int numberOfCorrectAnswerChoices) {
        String feedBack = "";
        if (question.getType().getType().equalsIgnoreCase("Multiple Choice") || question.getType().getType().equalsIgnoreCase("True or false")) {
            if (numberOfCorrectAnswerChoices == 0) {
                feedBack = feedbackConfig.getImmediateResponseIncorrectAnswer();
            } else {
                feedBack = feedbackConfig.getImmediateResponseCorrectAnswer();
            }
        }
        return feedBack;
    }


    private String immediateCorrectAnswerResponse(Question question, int numberOfCorrectAnswerChoices) {

        String feedBack = "";
        if (question.getType().getType().equalsIgnoreCase("Multiple Choice") || question.getType().getType().equalsIgnoreCase("True or false")) {
            if (numberOfCorrectAnswerChoices == 0) {
                String correctAnswer = findCorrectAnswer(question);
                feedBack = feedbackConfig.getImmediateCorrectAnswerResponseFeedbackIncorrectAnswer().replace("{correctAnswer}", correctAnswer);
            } else {
                feedBack = feedbackConfig.getImmediateCorrectAnswerResponseFeedbackCorrectAnswer();
            }
        }

        return feedBack;
    }


    private String immediateElaborated(Question question, int numberOfCorrectAnswerChoices) {
        String feedBack = "";
        if (question.getType().getType().equalsIgnoreCase("Multiple Choice") || question.getType().getType().equalsIgnoreCase("True or false")) {
            if (numberOfCorrectAnswerChoices == 0) {
                String correctAnswer = findCorrectAnswer(question);
                String feedbackTemplate = feedbackConfig.getImmediateElaborationFeedbackIncorrectAnswer();
                feedBack = feedbackTemplate.replace("{correctAnswer}", correctAnswer);
                feedBack = feedBack.replace("{explanation}", question.getExplanation());

            } else {
                String feedbackTemplate = feedbackConfig.getImmediateElaborationFeedbackCorrectAnswer();
                feedBack = feedbackTemplate.replace("{explanation}", question.getExplanation());
            }
        }

        return feedBack;
    }


    private String findCorrectAnswer(Question question) {
        for (Choice choice : question.getChoices()) {
            if (choice.isCorrect()) {
                return choice.getText();
            }
        }
        throw new IllegalStateException("No correct choice found for the question.");
    }

    @Override
    public List<Feedback> getFeedbackTypes() {
        return feedbackRepository.findAll();
    }

}
