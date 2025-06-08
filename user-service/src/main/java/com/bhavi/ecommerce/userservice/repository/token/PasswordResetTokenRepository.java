package com.bhavi.ecommerce.userservice.repository.token;

import com.bhavi.ecommerce.userservice.model.token.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Custom query method to find a token by its string value
    Optional<PasswordResetToken> findByToken(String token);

    // Custom query method to delete a token by user ID (useful if a user requests multiple resets)
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

}
