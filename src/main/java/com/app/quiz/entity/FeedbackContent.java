package com.app.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "feedback_content")
public class FeedbackContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_score")
    private Double minScore;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "overall_feedback")
    private String overallFeedback;

    @Column(name = "subtopic_feedback")
    private String subtopicFeedback;

}