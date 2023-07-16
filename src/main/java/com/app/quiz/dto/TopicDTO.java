package com.app.quiz.dto;

public record TopicDTO (
       Long id,
       String name,
       Integer numberOfQuestions,
       Integer easyQuestionsAvailable,
       Integer mediumQuestionsAvailable,
       Integer hardQuestionsAvailable
) { }
