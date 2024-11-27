package com.devfleming.e_commerce_bot.domain.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private String firstName;

    private String lastName;

    private String cellphone;

    private String cpf;
}
