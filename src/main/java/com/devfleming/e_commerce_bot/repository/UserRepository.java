package com.devfleming.e_commerce_bot.repository;

import com.devfleming.e_commerce_bot.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM telegram_user u WHERE u.cellphone = ?1", nativeQuery = true)
    User fetchByCellphone(String cellphone);

    @Query(value = "SELECT * FROM telegram_user u WHERE u.cpf = ?1", nativeQuery = true)
    User fetchByCpf(String cpf);
}
