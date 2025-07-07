package com.bhavi.ecommerce.productservice.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List; // Import List
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;

@RequiredArgsConstructor // For injecting JwtUtil
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    // abstract method in the OncePerRequestFilter abstract class
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Check for Authorization header and Bearer token format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue filter chain
            return; // No JWT or invalid format, move on
        }

        jwt = authHeader.substring(7); // Extract token after "Bearer "

        // 2. Validate the JWT token
        if (jwtUtil.validateToken(jwt)) {
            try {
                // Token is valid, extract claims
                Claims claims = jwtUtil.extractAllClaims(jwt);

                String username = claims.getSubject();

                // --- START OF CRITICAL CHANGE ---
                // The 'roles' claim is an ArrayList (JSON array) from the User Service
                // We need to retrieve it as a List<String>, not attempt to cast to String.
                @SuppressWarnings("unchecked") // Suppress warning for unchecked cast
                List<String> roles = (List<String>) claims.get("roles");

                // Convert each role string into a SimpleGrantedAuthority
                Collection<? extends GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                // --- END OF CRITICAL CHANGE ---

                // 3. Create authentication token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,    // principal (username)
                        null,        // credentials (not needed for JWT, already authenticated)
                        authorities  // authorities (roles)
                );

                // 4. Set authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Successfully authenticated user: {} with roles: {}", username, authorities);

            } catch (Exception e) {
                // Log any errors during token processing (e.g., malformed claims)
                log.error("Failed to set user authentication in security context: {}", e.getMessage());
                // Do NOT throw or return 401/403 here; let the filter chain continue.
                // The SecurityConfig's authorizeHttpRequests will eventually block unauthorized access.
            }
        } else {
            log.warn("Invalid JWT token provided.");
            // Authentication will not be set, leading to 401/403 if endpoint requires it.
        }

        // 5. Continue to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}