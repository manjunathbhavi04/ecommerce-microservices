package com.bhavi.ecommerce.userservice.controller;

import com.bhavi.ecommerce.userservice.dto.request.*;
import com.bhavi.ecommerce.userservice.dto.response.AuthResponse;
import com.bhavi.ecommerce.userservice.repository.UserRepository;
import com.bhavi.ecommerce.userservice.security.JwtUtil;
import com.bhavi.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController // Marks this as a REST controller
@RequestMapping("/api/auth") // Base URL for all endpoints in this controller
@RequiredArgsConstructor // Lombok: Generates constructor for final fields
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @PostMapping("/register")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest request) {

        AuthResponse response = userService.registerUser(request, frontendBaseUrl); // Pass frontendBaseUrl

        if (response.getToken() != null && !response.getToken().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getMessage() != null && response.getMessage().contains("already has the specified role")) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
//        if (authResponse.getMessage().contains("Invalid")) {
//            return new ResponseEntity<>(authResponse, HttpStatus.UNAUTHORIZED);
//        }
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.getEmail(), frontendBaseUrl);
        return ResponseEntity.ok("Password reset link sent to your email if the account exists.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully!");
    }

    // --- NEW ENDPOINT FOR EMAIL VERIFICATION ---
    @GetMapping("/verify-email") // Using GET as it's typically a direct link click
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        // In a real application, you might redirect to a success page on the frontend
        return ResponseEntity.ok("Email verified successfully! You can now log in.");
    }

    // You might also want an endpoint to resend the verification email
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody ForgotPasswordRequest request) { // Reusing DTO
        userService.resendVerificationEmail(request.getEmail(), frontendBaseUrl);
        return ResponseEntity.ok("Verification email resent if account exists and is not yet verified.");
    }

    // refresh token
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        AuthResponse response = userService.refreshAccessToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // Example secured endpoint (requires authentication)
    @GetMapping("/test-secured")
    public ResponseEntity<String> testSecuredEndpoint() {
        // This endpoint can only be accessed with a valid JWT
        return ResponseEntity.ok("You have accessed a secured endpoint!");
    }

    // Example endpoint requiring specific role (e.g., ADMIN)
    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ADMIN')") // Requires @EnableMethodSecurity in SecurityConfig
    public ResponseEntity<String> testAdminEndpoint() {
        // This endpoint can only be accessed by users with the ROLE_ADMIN authority
        return ResponseEntity.ok("Welcome, Admin! This is an admin-only endpoint.");
    }
}