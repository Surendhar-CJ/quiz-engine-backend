package com.app.quiz.utils;

public final class RegexPattern {

    public static final String EMAIL_PATTERN= "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]{3,20}+$";

    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$";
}