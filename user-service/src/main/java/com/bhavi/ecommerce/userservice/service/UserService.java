package com.bhavi.ecommerce.userservice.service;

import com.bhavi.ecommerce.userservice.dto.request.RegisterRequest;
import com.bhavi.ecommerce.userservice.dto.response.AuthResponse;
import com.bhavi.ecommerce.userservice.enums.Role;
import com.bhavi.ecommerce.userservice.model.User;
import com.bhavi.ecommerce.userservice.repository.UserRepository;
import com.bhavi.ecommerce.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Good practice for operations that modify data

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional // Ensures atomicity for database operations
    public AuthResponse registerUser(RegisterRequest request) {
        Optional<User> existingUserOptional = userRepository.findByEmail(request.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            // If user exists and already has the role, signal conflict
            if (existingUser.getRoles().contains(request.getRole())) {
                return AuthResponse.builder()
                        .message("User with this email already exists and already has the specified role.")
                        .build();
            } else {
                // User exists but wants to add a new role
                return addRoleToExistingUser(existingUser, request.getRole());
            }
        }

        // If user does not exist, create a new one
        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Assign the role from the request
        Set<Role> roles = new HashSet<>();
        roles.add(request.getRole()); // Use the role from the request DTO
        newUser.setRoles(roles);

        // Save the new user to the database
        User savedUser = userRepository.save(newUser);

        // Generate JWT for the newly registered user
        String token = jwtUtil.generateToken(savedUser); // UserDetails is based on savedUser roles

        return AuthResponse.builder()
                .token(token)
                .message("User registered successfully!")
                .userId(savedUser.getId())
                .userEmail(savedUser.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse addRoleToExistingUser(User user, Role newRole) {
        // Prevent adding duplicate roles
        if (user.getRoles().contains(newRole)) {
            return AuthResponse.builder()
                    .message("User already has the role: " + newRole.name())
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .build();
        }

        // Add the new role to the existing set of roles
        Set<Role> currentRoles = new HashSet<>(user.getRoles()); // Create a mutable copy
        currentRoles.add(newRole);
        user.setRoles(currentRoles);

        // Save the updated user
        User updatedUser = userRepository.save(user);

        // Generate a NEW token for the updated user.
        // This new token will now contain ALL of the user's roles (including the newly added one).
        String newToken = jwtUtil.generateToken(updatedUser);

        return AuthResponse.builder()
                .token(newToken) // Send the new token
                .message("Role '" + newRole.name() + "' added to user " + user.getEmail() + " successfully! Please re-login with this new token.")
                .userId(updatedUser.getId())
                .userEmail(updatedUser.getEmail())
                .build();
    }
}