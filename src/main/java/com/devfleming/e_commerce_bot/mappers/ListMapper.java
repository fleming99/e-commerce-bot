package com.devfleming.e_commerce_bot.mappers;

import com.devfleming.e_commerce_bot.domain.entities.Product;

import java.util.List;

public class ListMapper {

    public static StringBuilder mapListToStringBuilder(List<Product> productList){
        StringBuilder stringBuilder = new StringBuilder();
        for (Product product : productList){
            if (product.getActive() == 'A') {
                stringBuilder.append(String.format("ID:%d\nNome do Produto: %s\n" +
                                "Descrição: %s\n" +

                                "Tipo: %s\n\n",
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductDescription(),
                        product.getProductType()));
            }
        }
        return stringBuilder;
    }
}
