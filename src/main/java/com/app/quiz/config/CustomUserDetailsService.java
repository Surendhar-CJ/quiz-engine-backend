package com.app.quiz.config;

import com.app.quiz.entity.User;
import com.app.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



/**
 * Implementation of Spring Security's UserDetailsService interface.
 * This class is responsible for loading a user by their username from the database and constructing
 * a UserDetails object from the retrieved User object.
 *
 * @author Surendhar Chandran Jayapal
 */
@Service
public final class CustomUserDetailsService implements UserDetailsService {


    /**
     * UserRepository to interact with the User entity in the database.
     */
    private final UserRepository userRepository;



    /**
     * Constructs a new CustomUserDetailsService instance with the given UserRepository.
     *
     * @param userRepository the repository to retrieve User objects from the database.
     */
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    /**
     * Loads a user by their username and constructs a UserDetails object from the retrieved User object.
     *
     * @param username the username(email) of the user to load
     *
     * @return UserDetails object for the loaded user
     *
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepository.findUserByEmail(username);
        return new CustomUserDetails(user);
    }
}
