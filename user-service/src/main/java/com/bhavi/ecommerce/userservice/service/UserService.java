package com.bhavi.ecommerce.userservice.service;

import com.bhavi.ecommerce.userservice.dto.request.LoginRequest;
import com.bhavi.ecommerce.userservice.dto.request.RegisterRequest;
import com.bhavi.ecommerce.userservice.dto.response.AuthResponse;
import com.bhavi.ecommerce.userservice.enums.Role;
import com.bhavi.ecommerce.userservice.exception.InvalidTokenException;
import com.bhavi.ecommerce.userservice.exception.TokenExpiredException;
import com.bhavi.ecommerce.userservice.exception.UserNotFoundException;
import com.bhavi.ecommerce.userservice.model.User;
import com.bhavi.ecommerce.userservice.model.token.EmailVerificationToken;
import com.bhavi.ecommerce.userservice.model.token.PasswordResetToken;
import com.bhavi.ecommerce.userservice.model.token.RefreshToken;
import com.bhavi.ecommerce.userservice.repository.UserRepository;
import com.bhavi.ecommerce.userservice.repository.token.EmailVerificationTokenRepository;
import com.bhavi.ecommerce.userservice.repository.token.PasswordResetTokenRepository;
import com.bhavi.ecommerce.userservice.repository.token.RefreshTokenRepository;
import com.bhavi.ecommerce.userservice.security.JwtUtil;
import com.bhavi.ecommerce.userservice.service.email.EmailService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Good practice for operations that modify data

import java.time.Instant;
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
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService; // Inject EmailService
    private final PasswordResetTokenRepository passwordResetTokenRepository;// Injected from SecurityConfig
    private final RefreshTokenRepository refreshTokenRepository;


    @Transactional // Ensures atomicity for database operations
    public AuthResponse registerUser(RegisterRequest request, String frontendBaseUrl) {
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
                .isVerified(false)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Assign the role from the request
        Set<Role> roles = new HashSet<>();
        roles.add(request.getRole()); // Use the role from the request DTO
        newUser.setRoles(roles);

        // Save the new user to the database
        User savedUser = userRepository.save(newUser);

        // delete any old verification token for this user
        emailVerificationTokenRepository.deleteByUserId(savedUser.getId());

        // generate a unique token for email verification
        String verificationTokenString = UUID.randomUUID().toString();

        //set expiry date and time for the token
        LocalDateTime expiryDateTime = LocalDateTime.now().plusHours(24);

        // save the token for the user which is generated and can be used for the next 24 hrs
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .expiryDate(expiryDateTime)
                .user(savedUser)
                .token(verificationTokenString)
                .build();
        emailVerificationTokenRepository.save(emailVerificationToken);

        // 5. Construct the verification link
        // This link will point to your backend endpoint for verification
        String verificationLink = frontendBaseUrl + "/verify-email?token=" + verificationTokenString;

        // 6. Send the verification email
        String subject = "Verify Your E-commerce Account Email";
        String emailContent = "Hello " + savedUser.getFirstName() + ",\n\n"
                + "Thank you for registering with our E-commerce platform! "
                + "Please click the following link to verify your email address:\n\n"
                + verificationLink + "\n\n"
                + "This link will expire in 24 hours.\n"
                + "If you did not register for an account, please ignore this email.\n\n"
                + "Best regards,\nYour E-commerce Team";

        emailService.sendEmail(savedUser.getEmail(), subject, emailContent);

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
        // This new token will now contain ALL the user's roles (including the newly added one).
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

            // Find the actual User object to get userId and email for the response
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after successful authentication."));

            // Generate Access token (JWT)
            String token = jwtUtil.generateToken(userDetails);

            //Generate a Refresh Token and save it
            // first revoke any existing active refresh tokens for this user
            refreshTokenRepository.deleteByUserId(user.getId());

            //Generate refresh token
            String refreshTokenstring = UUID.randomUUID().toString();

            // set refresh token expiry 7 days from now
            Instant refreshTokenExpiryDate = Instant.now().plusSeconds(60 * 60 * 24 * 7);

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenstring)
                    .user(user)
                    .issuedAt(Instant.now())
                    .expiryDate(refreshTokenExpiryDate)
                    .revoked(false) //Initially not revoked
                    .build();
            refreshTokenRepository.save(refreshToken);

            return (AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshTokenstring)
                    .message("Login successful!")
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .build());

        } catch (Exception e) {
            // Handle authentication failure (e.g., bad credentials)
            return (AuthResponse.builder().message("Invalid email or password.").build());
        }
    }


    //To Refresh tokens
    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        // find the refresh token first
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new InvalidTokenException("Invalid Refresh Token"));

        // check if the token is expired or revoked
        if(refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh Token is revoked");
        }
        if(refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token has expired, Please log in again.");
        }

        User user = refreshToken.getUser();
        if(user == null) {
            throw new InvalidTokenException("Refresh Token is not associated with any user");
        }

        //Invalidate the old refresh token (Token rotation strategy)
        //this is the security best practice: once the refresh token is used, it should be replaced
        // this makes leaked refresh token useless after one use
        refreshToken.setRevoked(true);
        // mark the current as revoked and save it in the database
        refreshTokenRepository.save(refreshToken);

        //Generate new Access token
        String newAccessToken = jwtUtil.generateToken(user);

        String newRefreshToken = UUID.randomUUID().toString();

        Instant newRefreshTokenExpiryDate = Instant.now().plusSeconds(60 * 60 * 24 * 7);

        RefreshToken newRefreshToken1 = RefreshToken.builder()
                .token(newRefreshToken)
                .issuedAt(Instant.now())
                .expiryDate(newRefreshTokenExpiryDate)
                .user(user)
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken1);

        return AuthResponse.builder()
                .refreshToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .userEmail(user.getEmail())
                .message("Tokens refreshed successfully")
                .build();
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


    // email verification
    @Transactional
    public void verifyEmail(String token) {
        // find the email verification token
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() ->  new InvalidTokenException("Invalid or non-existent email verification token."));

        //check if the token has expired
        if (verificationToken.isExpired()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new TokenExpiredException("Email verification token has expired");
        }

        // get the associated user
        User user = verificationToken.getUser();
        if(user == null) {
            throw new InvalidTokenException("Email verification token is not associated with a user.");
        }

        //check if the user is already verified
        if(user.isEnabled()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Email is already verified for this account");
        }

        // verify the user's email
        user.setVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);
    }

    public void resendVerificationEmail(@NotBlank(message = "Email cannot be empty") @Email(message = "Invalid email format") String email, String frontendBaseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Invalid email, No user exist with the email"+email));

        if(user.isEnabled()) {
            throw new InvalidTokenException("Email is already verified for this account");
        }

        //we have to generate a new token to resend verification mail
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        // generate a unique token for email verification
        String verificationTokenString = UUID.randomUUID().toString();

        //set expiry date and time for the token
        LocalDateTime expiryDateTime = LocalDateTime.now().plusHours(24);

        // save the token for the user which is generated and can be used for the next 24 hrs
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .expiryDate(expiryDateTime)
                .user(user)
                .token(verificationTokenString)
                .build();
        emailVerificationTokenRepository.save(emailVerificationToken);

//        Send the email ---
        String verificationLink = frontendBaseUrl + "/verify-email?token=" + verificationTokenString;
        String subject = "Resend: Verify Your E-commerce Account Email";
        String emailContent = "Hello " + user.getFirstName() + ",\n\n"
                + "We received a request to resend your email verification. "
                + "Please click the following link to verify your email address:\n\n"
                + verificationLink + "\n\n"
                + "This link will expire in 24 hours.\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Best regards,\nYour E-commerce Team";
        emailService.sendEmail(user.getEmail(), subject, emailContent);
    }
}