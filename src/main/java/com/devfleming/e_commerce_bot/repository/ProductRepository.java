package com.devfleming.e_commerce_bot.repository;

import com.devfleming.e_commerce_bot.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
