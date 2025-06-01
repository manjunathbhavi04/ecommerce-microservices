package com.bhavi.ecommerce.userservice.service;

import com.bhavi.ecommerce.userservice.dto.request.LoginRequest;
import com.bhavi.ecommerce.userservice.dto.request.RegisterRequest;
import com.bhavi.ecommerce.userservice.dto.response.AuthResponse;
import com.bhavi.ecommerce.userservice.enums.Role;
import com.bhavi.ecommerce.userservice.exception.InvalidTokenException;
import com.bhavi.ecommerce.userservice.exception.TokenExpiredException;
import com.bhavi.ecommerce.userservice.exception.UserNotFoundException;
import com.bhavi.ecommerce.userservice.model.User;
import com.bhavi.ecommerce.userservice.model.token.PasswordResetToken;
import com.bhavi.ecommerce.userservice.repository.UserRepository;
import com.bhavi.ecommerce.userservice.repository.token.PasswordResetTokenRepository;
import com.bhavi.ecommerce.userservice.security.JwtUtil;
import com.bhavi.ecommerce.userservice.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Good practice for operations that modify data

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService; // Inject EmailService
    private final PasswordResetTokenRepository passwordResetTokenRepository;// Injected from SecurityConfig


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

    //Login
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // If authentication is successful, get UserDetails and generate JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // Find the actual User object to get userId and email for the response
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after successful authentication."));


            return (AuthResponse.builder()
                    .token(token)
                    .message("Login successful!")
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .build());

        } catch (Exception e) {
            // Handle authentication failure (e.g., bad credentials)
            return (AuthResponse.builder().message("Invalid email or password.").build());
        }
    }


    // FORGOT PASSWORD REQUEST ---
    @Transactional
    public void requestPasswordReset(String email, String frontendBaseUrl) {
        // 1. Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // 2. Delete any existing password reset tokens for this user
        // This ensures only one valid token exists at a time for a user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // 3. Generate a unique token
        String token = UUID.randomUUID().toString();

        // 4. Set token expiry (e.g., 1 hour from now)
        LocalDateTime expiryDateTime = LocalDateTime.now().plusHours(1);

        // 5. Create and save the new PasswordResetToken
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDateTime)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // 6. Construct the password reset link for the user
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token; // Adjust frontend path if needed

        // 7. Send the email
        String subject = "Password Reset Request for Your Account";
        String emailContent = "Hello " + user.getFirstName() + ",\n\n"
                + "We received a request to reset the password for your account. "
                + "Please click on the following link to reset your password:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in 1 hour.\n"
                + "If you did not request a password reset, please ignore this email.\n\n"
                + "Best regards,\nYour E-commerce Team";

        emailService.sendEmail(user.getEmail(), subject, emailContent);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {

        //find the token which was generated for the reset password
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or non-existent password reset token."));

        // check if the token is expired because maybe the customer has tried to reset the password long back,
        //and now he is trying to access the same token again because tokens are expired every hour
        if ( resetToken.isExpired() ) {
            //if the token is expired, then delete the whole token
            passwordResetTokenRepository.delete(resetToken);
            throw new TokenExpiredException(("Password reset token has expired"));
        }

        // get the user associated with the token because his password must be updated
        User user = resetToken.getUser();
        if (  user == null ) {
            throw new InvalidTokenException("Password reset token is not associated with a user.");
        }

        //now update the user's password and save it in the database
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate/Delete the token after successful password reset
        // This prevents the same token from being used multiple times
        passwordResetTokenRepository.delete(resetToken);
    }
}