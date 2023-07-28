package com.app.quiz.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name="user_feedback")
public class UserFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feedback_by_user_id")
    private Long feedbackByUserId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "feedback_for_user_id")
    private Long feedbackForUserId;


    public UserFeedback(Long feedbackByUserId, Long topicId, String comment, Long feedbackForUserId) {
        this.feedbackByUserId = feedbackByUserId;
        this.topicId = topicId;
        this.comment = comment;
        this.feedbackForUserId = feedbackForUserId;
    }
}
