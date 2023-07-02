package com.app.quiz.entity;

import com.app.quiz.utils.FeedbackType;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Transient
    private FeedbackType feedbackType;

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

    @Transient
    private Map<Question, List<Choice>> responses;



    public Quiz(User user, Topic topic, Boolean isCompleted, Double finalScore) {
        this.user = user;
        this.topic = topic;
        this.isCompleted = isCompleted;
        this.servedQuestions = new ArrayList<>();
        this.responses = new HashMap<>();
        this.finalScore = finalScore;
    }

    public Quiz() {
        this.servedQuestions = new ArrayList<>();
        this.responses = new HashMap<>();
    }

    public boolean quizCompleted() {
        return this.getTopic().getQuestionsList().size() == this.getServedQuestions().size();
    }

}
