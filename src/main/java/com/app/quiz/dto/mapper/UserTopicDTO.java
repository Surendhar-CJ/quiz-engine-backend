package com.app.quiz.dto.mapper;

public record UserTopicDTO(
        Long id,
        String name,
        Double rating,
        Integer numberOfRaters

) {
}
