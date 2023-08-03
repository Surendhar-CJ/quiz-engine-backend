package com.app.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "score")
    private Double score;

    @Column(name = "explanation")
    private String explanation;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    @ToString.Exclude
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "subtopic_id")
    private Subtopic subtopic;

    @ManyToOne
    @JoinColumn(name = "question_type_id")
    private QuestionType type;

    @ManyToOne
    @JoinColumn(name = "difficulty_level_id")
    private DifficultyLevel difficultyLevel;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Choice> choices;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    public Question(String text, QuestionType type, Double score, Topic topic, Subtopic subtopic, DifficultyLevel difficultyLevel, List<Choice> choices, User user) {
        this.text = text;
        this.type = type;
        this.score = score;
        this.topic = topic;
        this.subtopic = subtopic;
        this.difficultyLevel = difficultyLevel;
        this.choices = new ArrayList<>();
        this.user = user;
        this.isDeleted = false;
    }

    public Question() {
        this.choices = new ArrayList<>();
    }


}
