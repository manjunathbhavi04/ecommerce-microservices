package com.bhavi.ecommerce.productservice.service;

import com.bhavi.ecommerce.productservice.dto.request.ProductRequest;
import com.bhavi.ecommerce.productservice.dto.response.ProductResponse;
import com.bhavi.ecommerce.productservice.exception.ProductAlreadyExistsException;
import com.bhavi.ecommerce.productservice.exception.ProductNotFoundException;
import com.bhavi.ecommerce.productservice.model.Product;
import com.bhavi.ecommerce.productservice.repository.ProductRepository;
import com.bhavi.ecommerce.productservice.transformer.ProductTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    //create or add a product
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Attempting to create a product: {}", request.getName());

        if(productRepository.findByName(request.getName()).isPresent()) {
            throw new ProductAlreadyExistsException("Product with name "+request.getName()+" already exists");
        }

        Product product = ProductTransformer.productRequestToProduct(request);

        Product savedProduct  = productRepository.save(product);
        productRepository.save(savedProduct);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return ProductTransformer.productToProductResponse(savedProduct);
    }

    // GET THE PRODUCT WITH ID
    @Transactional(readOnly = true) // Optimize for read-only operations
    public ProductResponse getProductById(Long id) {
        log.info("Attempting to retrieve product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        log.info("Product retrieved successfully with ID: {}", id);
        return ProductTransformer.productToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Attempting to retrieve all products (Page: {}, Size: {}, SortBy: {}, SortDir: {})", pageNo, pageSize, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Product> productsPage = productRepository.findAll(pageable);
        log.info("Retrieved {} products on page {} out of {} total pages.", productsPage.getNumberOfElements(), productsPage.getNumber(), productsPage.getTotalPages());

        return productsPage.map(ProductTransformer::productToProductResponse);
    }


    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Attempting to update product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        // Check if the name we are changing also already exists
        // here we also have to know that we are not only updating name this case is only for name
        Optional<Product> productWithSameName = productRepository.findByName(request.getName());

        // check the id of the request and product with the same name if same id then we are updating different attributes
        if(productWithSameName.isPresent() && !productWithSameName.get().getId().equals(id)) {
            throw new ProductAlreadyExistsException("Another product with name '" + request.getName() + "' already exists.");
        }

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setImageUrl(request.getImageUrl());
        existingProduct.setCategory(request.getCategory());
        // onUpdate lifecycle callback will set updatedAt timestamp

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product with ID: {} updated successfully.", updatedProduct.getId());
        return ProductTransformer.productToProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Attempting to delete product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        productRepository.delete(product);
        log.info("Product with ID: {} deleted successfully.", id);
    }

}
