package com.bhavi.ecommerce.orderservice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j // Lombok annotation for logging
public class JwtUtil {

    // Inject the JWT secret from application.properties
    @Value("${jwt.secret}")
    private String SECRET;

    // Extracts the username (subject) from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extracts all claims (payload) from the token
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignedKey()) // Use the secret key to parse and verify the token
                .build()
                .parseClaimsJws(token) // Parses the JWS (Signed JWT)
                .getBody(); // Gets the claims payload
    }

    // Checks if the token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validates the token's signature and expiration
    public Boolean validateToken(String token) {
        try {
            // Checks if the token is not expired. Signature verification is implicitly done by extractAllClaims().
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Log specific JWT validation errors (e.g., signature mismatch, malformed token)
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    // Decodes the base64-encoded secret and creates a signing key
    private Key getSignedKey() {
        // Logging sensitive information (like SECRET) at INFO level is a security risk.
        // Changed to DEBUG level, so it won't appear in default production logs.
        log.debug("Attempting to decode JWT Secret.");
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}