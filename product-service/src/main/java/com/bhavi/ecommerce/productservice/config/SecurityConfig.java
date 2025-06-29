package com.bhavi.ecommerce.productservice.config;

import com.bhavi.ecommerce.productservice.security.jwt.JwtAuthenticationFilter;
import com.bhavi.ecommerce.productservice.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Marks this class as a Spring configuration class
@EnableWebSecurity // Enables Spring Security's web security features
@RequiredArgsConstructor // For injecting JwtUtil
public class SecurityConfig {

    private final JwtUtil jwtUtil; // Used to create JwtAuthenticationFilter

    // Define the custom JWT filter as a Bean
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    // Configure the security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless REST APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions (no HttpSession)
                .authorizeHttpRequests(authorize -> authorize
                        // Publicly accessible endpoints (no authentication/authorization needed)
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // Anyone can view products

                        // Endpoints requiring ADMIN role
                        .requestMatchers(HttpMethod.POST, "/api/products/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                );

        // Add our custom JWT filter before Spring Security's default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}