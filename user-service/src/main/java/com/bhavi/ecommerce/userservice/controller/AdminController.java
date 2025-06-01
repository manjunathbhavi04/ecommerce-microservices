package com.bhavi.ecommerce.userservice.controller;

import com.bhavi.ecommerce.userservice.dto.request.RegisterRequest; // Now using RegisterRequest
import com.bhavi.ecommerce.userservice.dto.response.AuthResponse; // To return proper response
import com.bhavi.ecommerce.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin") // Base URL for admin specific endpoints
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * Endpoint for ADMINs to create new users with specific roles,
     * or to add new roles to existing users.
     * This leverages the existing registerUser logic in UserService.
     * The token returned will reflect the user's *new* total set of roles.
     * Note: If creating a new user, the password will be encoded.
     */
    @PostMapping("/users") // e.g., POST http://localhost:8081/api/admin/users
    @PreAuthorize("hasRole('ADMIN')") // ONLY users with 'ROLE_ADMIN' can access this endpoint
    public ResponseEntity<AuthResponse> createOrAddRoleToUser(@RequestBody RegisterRequest request) {
        // The userService.registerUser method now handles:
        // 1. Creating a new user with the specified role if the email doesn't exist.
        // 2. Adding the specified role to an existing user if the email exists but role is new.
        AuthResponse response = userService.registerUser(request);

        // Check if the operation was successful (e.g., token generated)
        if (response.getToken() != null && !response.getToken().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getMessage() != null && response.getMessage().contains("already has the specified role")) {
            // Specific case for user already having the role
            return ResponseEntity.status(HttpStatus.OK).body(response); // 200 OK as no change was made
        }
        // Fallback for other scenarios, like email already exists but not for the specific role
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }

    // You can add more admin-specific endpoints here, e.g.,
    // @GetMapping("/users") to get all users
    // @DeleteMapping("/users/{id}") to delete a user
    // @PutMapping("/users/{id}/roles") to specifically update roles (more fine-grained control)
    // @PreAuthorize("hasRole('ADMIN')")
    // @GetMapping("/test-admin-only")
    // public ResponseEntity<String> testAdminOnly() {
    //     return ResponseEntity.ok("This endpoint is accessible only by ADMINs.");
    // }
}