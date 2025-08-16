package com.bhavi.ecommerce.orderservice.exception;

import com.bhavi.ecommerce.orderservice.dto.response.ApiResponse;
import com.bhavi.ecommerce.orderservice.dto.response.ProductResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse> OrderNotFound(OrderNotFoundException e) {
        ApiResponse response = new ApiResponse(e.getMessage(), false);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse> productNotFound(ProductNotFoundException e) {
        ApiResponse response = new ApiResponse(e.getMessage(), false);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientProductStock.class)
    public ResponseEntity<ApiResponse> insufficientProductStock(InsufficientProductStock e) {
        ApiResponse response = new ApiResponse(e.getMessage(), false);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}
