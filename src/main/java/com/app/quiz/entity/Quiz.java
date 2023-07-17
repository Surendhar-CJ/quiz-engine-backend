package com.app.quiz.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    private Feedback feedbackType;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "questions_limit")
    private Integer questionsLimit;

    @ManyToOne
    @JoinColumn(name = "difficulty_level_id")
    private DifficultyLevel difficultyLevel;

    @Column(name = "final_score" )
    private Double finalScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OrderColumn(name = "question_order")
    @ManyToMany
    @JoinTable(
            name = "quiz_question",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> servedQuestions;

    @OneToMany(mappedBy = "quiz")
    private List<Response> responses;



    public Quiz(User user, Topic topic, Feedback feedbackType, Boolean isCompleted, Integer questionsLimit, DifficultyLevel difficultyLevel, Double finalScore, LocalDateTime createdAt) {
        this.user = user;
        this.topic = topic;
        this.feedbackType = feedbackType;
        this.isCompleted = isCompleted;
        this.questionsLimit = questionsLimit;
        this.difficultyLevel = difficultyLevel;
        this.finalScore = finalScore;
        this.createdAt = createdAt;
        this.servedQuestions = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public Quiz() {
        this.servedQuestions = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public boolean quizCompleted() {
        return this.getTopic().getQuestionsList().size() == this.getServedQuestions().size();
    }

}
