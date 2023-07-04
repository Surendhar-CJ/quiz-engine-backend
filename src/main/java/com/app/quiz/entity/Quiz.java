package com.app.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    private Feedback feedbackType;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "final_score" )
    private Double finalScore;

    @ManyToMany
    @JoinTable(
            name = "quiz_question",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> servedQuestions;

    @OneToMany(mappedBy = "quiz")
    private List<Response> responses;



    public Quiz(User user, Topic topic, Feedback feedbackType, Boolean isCompleted, Double finalScore) {
        this.user = user;
        this.topic = topic;
        this.feedbackType = feedbackType;
        this.isCompleted = isCompleted;
        this.servedQuestions = new ArrayList<>();
        this.responses = new ArrayList<>();
        this.finalScore = finalScore;
    }

    public Quiz() {
        this.servedQuestions = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public boolean quizCompleted() {
        return this.getTopic().getQuestionsList().size() == this.getServedQuestions().size();
    }

}
