package com.bhavi.ecommerce.userservice.repository.token;

import com.bhavi.ecommerce.userservice.model.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    Optional<RefreshToken> findByUserIdAndRevokedFalseAndExpiryDateAfter(Long userId, Instant now);
}
