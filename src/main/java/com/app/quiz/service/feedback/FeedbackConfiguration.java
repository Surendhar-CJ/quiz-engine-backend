package com.app.quiz.service.feedback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:feedback.properties")
public class FeedbackConfiguration {

    @Value("${IMMEDIATE_RESPONSE.CORRECT_ANSWER}")
    private String immediateResponseCorrectAnswer;

    @Value("${IMMEDIATE_RESPONSE.INCORRECT_ANSWER}")
    private String immediateResponseIncorrectAnswer;

    @Value("${IMMEDIATE_CORRECT_ANSWER_RESPONSE_FEEDBACK.CORRECT_ANSWER}")
    private String immediateCorrectAnswerResponseFeedbackCorrectAnswer;

    @Value("${IMMEDIATE_CORRECT_ANSWER_RESPONSE_FEEDBACK.INCORRECT_ANSWER}")
    private String immediateCorrectAnswerResponseFeedbackIncorrectAnswer;

    @Value("${IMMEDIATE_ELABORATION_FEEDBACK.CORRECT_ANSWER}")
    private String immediateElaborationFeedbackCorrectAnswer;

    @Value("${IMMEDIATE_ELABORATION_FEEDBACK.INCORRECT_ANSWER}")
    private String immediateElaborationFeedbackIncorrectAnswer;

    // getters
    public String getImmediateResponseCorrectAnswer() {
        return immediateResponseCorrectAnswer;
    }

    public String getImmediateResponseIncorrectAnswer() {
        return immediateResponseIncorrectAnswer;
    }

    public String getImmediateCorrectAnswerResponseFeedbackCorrectAnswer() {
        return immediateCorrectAnswerResponseFeedbackCorrectAnswer;
    }

    public String getImmediateCorrectAnswerResponseFeedbackIncorrectAnswer() {
        return immediateCorrectAnswerResponseFeedbackIncorrectAnswer;
    }

    public String getImmediateElaborationFeedbackCorrectAnswer() {
        return immediateElaborationFeedbackCorrectAnswer;
    }

    public String getImmediateElaborationFeedbackIncorrectAnswer() {
        return immediateElaborationFeedbackIncorrectAnswer;
    }
}
