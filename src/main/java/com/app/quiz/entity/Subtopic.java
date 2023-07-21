package com.app.quiz.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@Table(name="subtopic")
public class Subtopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long Id;

    @Column(name="name")
    private String name;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "topic_id")
    private Topic topic;

    public Subtopic(String name) {
        this.name = name;
    }
}


