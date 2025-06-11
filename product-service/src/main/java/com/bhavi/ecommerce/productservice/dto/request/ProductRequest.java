package com.bhavi.ecommerce.productservice.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 100, message = "Product name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Product description cannot be empty")
    @Size(max = 1000, message = "Product description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Product price cannot be null")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Product price format invalid (max 8 digits, 2 decimal places)")
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Pattern(regexp = "^(http|https)://.*\\.(jpeg|jpg|gif|png|webp)$", message = "Invalid image URL format")
    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    @Nullable // Allows the image URL to be null
    private String imageUrl;

    @NotBlank(message = "Category cannot be empty")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
}
