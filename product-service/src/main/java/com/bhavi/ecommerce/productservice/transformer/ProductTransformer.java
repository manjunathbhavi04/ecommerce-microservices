package com.bhavi.ecommerce.productservice.transformer;

import com.bhavi.ecommerce.productservice.dto.request.ProductRequest;
import com.bhavi.ecommerce.productservice.dto.response.ProductResponse;
import com.bhavi.ecommerce.productservice.model.Product;

public class ProductTransformer {

    public static Product productRequestToProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .build();
    }

    public static ProductResponse productToProductResponse(Product savedProduct) {
        return ProductResponse.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .category(savedProduct.getCategory())
                .createdAt(savedProduct.getCreatedAt())
                .description(savedProduct.getDescription())
                .imageUrl(savedProduct.getImageUrl())
                .price(savedProduct.getPrice())
                .stockQuantity(savedProduct.getStockQuantity())
                .updatedAt(savedProduct.getUpdatedAt())
                .build();
    }
}
