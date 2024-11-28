package com.devfleming.e_commerce_bot.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", unique = true, nullable = false, updatable = false)
    private Long productId;

    @Column(name = "product_name", length = 50, nullable = false)
    private String productName;

    @Column(name = "product_description", length = 100, nullable = false)
    private String productDescription;

    @Column(name = "product_type", length = 50, nullable = false)
    private String productType;

    @Column(name = "active", length = 1, nullable = false)
    private char active;
}
