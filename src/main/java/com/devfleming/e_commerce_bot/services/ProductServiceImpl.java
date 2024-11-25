package com.devfleming.e_commerce_bot.services;

import com.devfleming.e_commerce_bot.domain.dto.ProductDto;
import com.devfleming.e_commerce_bot.domain.entities.Product;
import com.devfleming.e_commerce_bot.domain.usecases.ProductService;
import com.devfleming.e_commerce_bot.mappers.ProductMapper;
import com.devfleming.e_commerce_bot.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product createNewProduct(ProductDto productDto) {
        return productRepository.save(ProductMapper.mapToProduct(productDto));
    }

    @Override
    public Product fetchSingleProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Cannot find the product by id: " + productId));
    }

    @Override
    public List<Product> fetchProductsList() {
        return productRepository.findAll();
    }

    @Override
    public void inactivateProductById(Long productId) {

    }
}
