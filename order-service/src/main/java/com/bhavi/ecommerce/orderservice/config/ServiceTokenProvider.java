package com.bhavi.ecommerce.orderservice.config;

import com.bhavi.ecommerce.orderservice.dto.request.LoginRequest;
import com.bhavi.ecommerce.orderservice.dto.response.AuthResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient; // Keep this import

@Component
@Getter
@Slf4j
public class ServiceTokenProvider {

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${orderservice.client.email}")
    private String serviceEmail;

    @Value("${orderservice.client.password}")
    private String servicePassword;

    private volatile String serviceJwtToken;
    private WebClient userServiceLoginWebClient; // Declare the WebClient instance

    // Constructor no longer takes WebClient.Builder
    public ServiceTokenProvider() {
        // Default constructor
    }

    @PostConstruct
    public void init() {
        log.info("ServiceTokenProvider: Initializing WebClient for User Service login.");
        // Create the WebClient instance directly here for login purposes
        this.userServiceLoginWebClient = WebClient.builder() // <--- Direct WebClient.builder() call
                .baseUrl(userServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("ServiceTokenProvider: Attempting to obtain service JWT token from User Service: {}", userServiceUrl);
        try {
            this.serviceJwtToken = userServiceLoginWebClient.post() // Use the newly created WebClient
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new LoginRequest(serviceEmail, servicePassword))
                    .retrieve()
                    .bodyToMono(AuthResponse.class)
                    .map(AuthResponse::getToken)
                    .block();

            log.info("ServiceTokenProvider: Successfully obtained service JWT token for inter-service communication.");
        } catch (Exception e) {
            log.error("ServiceTokenProvider: Failed to obtain service JWT token from User Service: {}. Inter-service calls may fail.", e.getMessage(), e);
            // Re-throw as RuntimeException to prevent application startup if token acquisition fails
            throw new RuntimeException("ServiceTokenProvider: Failed to initialize inter-service JWT token. Please check user service URL, credentials, or network connectivity.", e);
        }
    }
}