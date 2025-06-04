package com.bhavi.ecommerce.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String message;
    private Long userId; // Optionally include user ID
    private String userEmail; // Optionally include user email
    // might add user roles here too
}
