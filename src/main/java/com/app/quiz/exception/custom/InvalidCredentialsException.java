package com.app.quiz.exception.custom;

public class InvalidCredentialsException extends RuntimeException{

   public InvalidCredentialsException(String message) {
       super(message);
   }
}
