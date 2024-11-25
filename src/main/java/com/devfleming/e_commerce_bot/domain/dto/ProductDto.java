package com.devfleming.e_commerce_bot.domain.dto;

import lombok.*;

@Getter @Setter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private String productName;

    private String productDescription;

    private String productType;
}
