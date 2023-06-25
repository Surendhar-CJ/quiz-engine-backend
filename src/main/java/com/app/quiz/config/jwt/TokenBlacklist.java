package com.app.quiz.config.jwt;

import com.app.quiz.exception.custom.InvalidCredentialsException;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;


/**
 * TokenBlackList class is used to check and store the blacklisted tokens if the user logs out.
 *
 * @author Surendhar Chandran Jayapal
 */
@Component
public class TokenBlacklist {


    /**
     * Set to store all the blacklisted tokens
     */
    private final Set<String> blacklistedTokens = new HashSet<>();





    /**
     * This method add a token to the blacklistedTokens Set.
     *
     * @param token token to be blacklisted
     */
    public void addTokenToBlacklist(String token) {
        blacklistedTokens.add(validateToken(token));
    }





    /**
     * This method validates if the token is blacklisted or not
     *
     * @param token token to be validated
     *
     * @return true if the token is blacklisted, else false
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(validateToken(token));
    }





    /**
     * This method validates the token and returns the user object.
     *
     * @param token JWT that needs to be validated
     *
     * @return the validated token
     */
    private String validateToken(String token)
    {
        if(token == null || token.length() == 0 || token.matches("(?i)null")) {
            throw new InvalidCredentialsException("Invalid token");
        }
        return token;
    }



}
