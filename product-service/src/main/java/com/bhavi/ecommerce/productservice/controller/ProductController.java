package com.bhavi.ecommerce.productservice.controller;

import com.bhavi.ecommerce.productservice.dto.request.ProductRequest;
import com.bhavi.ecommerce.productservice.dto.response.ApiResponse;
import com.bhavi.ecommerce.productservice.dto.response.ProductResponse;
import com.bhavi.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    // Create a Product
    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("Received request to create product: {}", request.getName());
        ProductResponse createdProduct = productService.createProduct(request);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // Get Page by its ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id) {
        log.info("Received request to get product by ID: {}", id);
        ProductResponse product = productService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    // Get all Products using pagination
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        log.info("Received request to get all products (Page: {}, Size: {}, SortBy: {}, SortDir: {})", pageNo, pageSize, sortBy, sortDir);
        Page<ProductResponse> productsPage = productService.getAllProducts(pageNo, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }


    // Update Product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest request) {
        log.info("Received request to update product ID: {}", id);
        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    // Delete a Product with its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable("id") Long id) {
        log.info("Received request to delete product ID: {}", id);
        productService.deleteProduct(id);
        ApiResponse response = new ApiResponse("Product with ID: " + id + " deleted successfully.", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<Void> decreaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.decreaseStocks(id, quantity);
        return ResponseEntity.ok().build();
    }

}
