package com.app.quiz.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int httpStatusCode;
    private String message;
}
