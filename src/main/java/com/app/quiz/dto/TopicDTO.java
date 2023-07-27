package com.app.quiz.dto;

import com.app.quiz.entity.Subtopic;

import java.util.List;

public record TopicDTO (
       Long id,
       String name,
       String userName,
       Double rating,
       Integer numberOfRaters,
       Integer numberOfQuestions,
       Integer easyQuestionsAvailable,
       Integer mediumQuestionsAvailable,
       Integer hardQuestionsAvailable,
       List<Subtopic> subtopics
) { }
