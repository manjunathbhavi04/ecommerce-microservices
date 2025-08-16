package com.bhavi.ecommerce.orderservice.service.client;

import com.bhavi.ecommerce.orderservice.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
public class ProductServiceClient {

    private final WebClient webClient;

    // Modified constructor: now accepts a pre-configured WebClient directly
    public ProductServiceClient(WebClient webClient) {
        this.webClient = webClient;
        log.info("ProductServiceClient initialized with its WebClient.");
    }

    public Mono<ProductResponseDto> getProductDetails(Long productId) {
        log.debug("Fetching product details for productId: {}", productId);
        return webClient.get()
                .uri("/api/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, // Custom error handling for 404
                        response -> {
                            log.warn("Product not found: productId={}", productId);
                            return Mono.empty(); // Return empty Mono for 404
                        })
//                .onStatus(HttpStatus::isError, // Handle all other 4xx or 5xx errors
//                        response -> response.bodyToMono(String.class)
//                                .flatMap(errorBody -> {
//                                    log.error("Error response from Product Service for product {}: Status={}, Body={}",
//                                            productId, response.statusCode(), errorBody);
//                                    return Mono.error(() -> new RuntimeException("Error from Product Service: " + errorBody));
//                                }))
                .bodyToMono(ProductResponseDto.class) // Convert response body to ProductResponseDto
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("WebClient error fetching product details for productId {}: Status={}, Message={}",
                                productId, ex.getStatusCode(), ex.getMessage()));
    }

    //Mono is used when you want to return one element
    public Mono<Void> decreaseProductStock(Long productId, Integer quantity) {
        log.debug("Decreasing stock for productId: {} by quantity: {}", productId, quantity);
        // Assuming Product Service has an endpoint like PUT /api/products/{id}/decrease-stock?quantity=X
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products/{id}/decrease-stock")
                        .queryParam("quantity", quantity) // Pass quantity as a query parameter
                        .build(productId))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, // Custom error handling for 404
                        response -> {
                            log.warn("Product not found: productId={}", productId);
                            return Mono.empty(); // Return empty Mono for 404
                        })
//                .onStatus(HttpStatus::isError, // Handle all 4xx or 5xx errors
//                        response -> response.bodyToMono(String.class)
//                                .flatMap(errorBody -> {
//                                    log.error("Error response from Product Service during stock decrease for product {}: Status={}, Body={}",
//                                            productId, response.statusCode(), errorBody);
//                                    // You might want more specific exceptions here (e.g., InsufficientStockException)
//                                    return Mono.error(new RuntimeException("Product Service stock update failed: " + errorBody));
//                                }))
                .bodyToMono(Void.class) // Expect no body in response, just status
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("WebClient error decreasing stock for productId {}: Status={}, Message={}",
                                productId, ex.getStatusCode(), ex.getMessage()));
    }

}
