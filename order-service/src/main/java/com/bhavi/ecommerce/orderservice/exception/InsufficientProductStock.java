package com.bhavi.ecommerce.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INSUFFICIENT_STORAGE)
public class InsufficientProductStock extends RuntimeException {
    public InsufficientProductStock(String message) {
        super(message);
    }
}
