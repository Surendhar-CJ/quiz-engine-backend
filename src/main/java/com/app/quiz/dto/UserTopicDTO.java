package com.app.quiz.dto;

public record UserTopicDTO(
        Long id,
        String name,
        Double rating,
        Integer numberOfRaters

) {
}
