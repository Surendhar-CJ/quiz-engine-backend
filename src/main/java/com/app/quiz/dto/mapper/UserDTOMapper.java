package com.app.quiz.dto.mapper;

import com.app.quiz.dto.UserDTO;
import com.app.quiz.entity.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public final class UserDTOMapper implements Function<User, UserDTO> {

    @Override
    public UserDTO apply(User user) {
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
