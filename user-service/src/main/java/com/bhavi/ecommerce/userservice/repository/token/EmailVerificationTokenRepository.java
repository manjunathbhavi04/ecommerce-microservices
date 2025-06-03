package com.bhavi.ecommerce.userservice.repository.token;

import com.bhavi.ecommerce.userservice.model.token.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Mark this as a Spring Data JPA repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    // Custom query method to find a token by its string value
    Optional<EmailVerificationToken> findByToken(String token);

    // Custom query method to delete a token by user ID (useful if a user re-requests verification)
    void deleteByUserId(Long userId);
}