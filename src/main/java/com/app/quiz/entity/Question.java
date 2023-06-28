package com.app.quiz.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "question_type_id")
    private QuestionType type;

    @ManyToOne
    @JoinColumn(name = "difficulty_level_id")
    private DifficultyLevel difficultyLevel;

    @OneToMany(mappedBy = "question")
    private List<Choice> choices;

    public Question(String text, QuestionType type, Topic topic, DifficultyLevel difficultyLevel, List<Choice> choices) {
        this.text = text;
        this.type = type;
        this.topic = topic;
        this.difficultyLevel = difficultyLevel;
        this.choices = new ArrayList<>();
    }


}