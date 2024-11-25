package com.devfleming.e_commerce_bot.domain.usecases;

import com.devfleming.e_commerce_bot.domain.dto.ProductDto;
import com.devfleming.e_commerce_bot.domain.entities.Product;

import java.util.List;

public interface ProductService {

    Product createNewProduct(ProductDto productDto);

    Product fetchSingleProductById(Long productId);

    List<Product> fetchProductsList();

    void inactivateProductById(Long productId);
}
