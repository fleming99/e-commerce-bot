package com.devfleming.e_commerce_bot.services;

import com.devfleming.e_commerce_bot.domain.dto.UserDto;
import com.devfleming.e_commerce_bot.domain.entities.User;
import com.devfleming.e_commerce_bot.domain.usecases.UserService;
import com.devfleming.e_commerce_bot.mappers.UserMapper;
import com.devfleming.e_commerce_bot.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public User createNewUser(UserDto userDto) {
        return userRepository.save(UserMapper.mapToUser(userDto));
    }

    @Override
    public User fetchByCellphone(String cellphone) {
        return userRepository.fetchByCellphone(cellphone);
    }

    @Override
    public User fetchByCpf(String cpf) {
        return userRepository.fetchByCpf(cpf);
    }
}
