package com.bhavi.ecommerce.userservice.controller;

import com.bhavi.ecommerce.userservice.dto.request.ForgotPasswordRequest;
import com.bhavi.ecommerce.userservice.dto.response.AuthResponse;
import com.bhavi.ecommerce.userservice.dto.request.LoginRequest;
import com.bhavi.ecommerce.userservice.dto.request.RegisterRequest;
import com.bhavi.ecommerce.userservice.enums.Role;
import com.bhavi.ecommerce.userservice.model.User;
import com.bhavi.ecommerce.userservice.repository.UserRepository;
import com.bhavi.ecommerce.userservice.security.JwtUtil;
import com.bhavi.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController // Marks this as a REST controller
@RequestMapping("/api/auth") // Base URL for all endpoints in this controller
@RequiredArgsConstructor // Lombok: Generates constructor for final fields
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.registerUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
        if (authResponse.getMessage().contains("Invalid")) {
            return new ResponseEntity<>(authResponse, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String frontendBaseUrl = "http://localhost:5173"; // <-- CHANGE THIS TO YOUR ACTUAL FRONTEND BASE URL

        userService.requestPasswordReset(request.getEmail(), frontendBaseUrl);
        return ResponseEntity.ok("Password reset link sent to your email if the account exists.");
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