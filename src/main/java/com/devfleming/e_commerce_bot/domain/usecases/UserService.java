package com.devfleming.e_commerce_bot.domain.usecases;

import com.devfleming.e_commerce_bot.domain.dto.UserDto;
import com.devfleming.e_commerce_bot.domain.entities.User;

public interface UserService {

    User createNewUser(UserDto userDto);

    User fetchByCellphone(String cellphone);

    User fetchByCpf(String cpf);
}
