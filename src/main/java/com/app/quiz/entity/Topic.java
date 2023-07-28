package com.app.quiz.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "topic")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name= "user_id")
    private User user;

    @Column(name = "rating")
    private Double rating;

    @JsonIgnore
    @OneToMany(mappedBy = "topic")
    private List<Question> questionsList;

    @Transient
    private Integer numberOfQuestions;

    public Topic(String name, User user) {
        this.name = name;
        this.user = user;
        this.rating = 0.0;
        this.questionsList = new ArrayList<>();
        this.numberOfQuestions = 0;
    }

}
