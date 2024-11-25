package com.devfleming.e_commerce_bot.mappers;

import com.devfleming.e_commerce_bot.domain.dto.ProductDto;
import com.devfleming.e_commerce_bot.domain.entities.Product;

public class ProductMapper {

    public static Product mapToProduct(ProductDto productDto){
        return Product.builder()
                .productName(productDto.getProductName())
                .productDescription(productDto.getProductDescription())
                .productType(productDto.getProductType())
                .build();
    }

    public static ProductDto mapToProductDto(Product product){
        return ProductDto.builder()
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productType(product.getProductType())
                .build();
    }
}
