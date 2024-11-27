package com.devfleming.e_commerce_bot.mappers;

import com.devfleming.e_commerce_bot.domain.dto.UserDto;
import com.devfleming.e_commerce_bot.domain.entities.User;

public class UserMapper {

    public static User mapToUser(UserDto userDto){
        return User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .cellphone(userDto.getCellphone())
                .cpf(userDto.getCpf())
                .build();
    }

    public static UserDto mapToUserDto(User user){
        return UserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .cellphone(user.getCellphone())
                .cpf(user.getCpf())
                .build();
    }
}
