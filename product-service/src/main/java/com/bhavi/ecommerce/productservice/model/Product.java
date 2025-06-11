package com.bhavi.ecommerce.productservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder // Provides a builder pattern for object creation
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the product

    @Column(nullable = false, unique = true, length = 100)
    private String name; // Name of the product

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // Detailed description of the product

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Price of the product (BigDecimal for precision)

    @Column(nullable = false)
    private Integer stockQuantity; // Current stock level

    @Column(length = 255)
    private String imageUrl; // URL to the product's main image

    @Column(nullable = false, length = 50)
    private String category; // Product category (e.g., "Electronics", "Clothing")

    @Column(nullable = false)
    private LocalDateTime createdAt; // Timestamp when the product was added

    @Column(nullable = false)
    private LocalDateTime updatedAt; // Timestamp when the product was last updated

    // Lifecycle callbacks for timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}