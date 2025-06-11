package com.bhavi.ecommerce.productservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success = false; // Default to false for error responses

    public ApiResponse(String message) {
        this.message = message;
    }

}