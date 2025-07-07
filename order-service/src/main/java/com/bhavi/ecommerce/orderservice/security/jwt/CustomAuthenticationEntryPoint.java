package com.bhavi.ecommerce.orderservice.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        //content type json
        response.setContentType("application/json");
        // 401 unauthorized
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

        HashMap<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new Date());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED); // HTTP status code
        errorDetails.put("error", "Unauthorized"); // General error message
        errorDetails.put("message", authException.getMessage()); // Specific exception message
        errorDetails.put("path", request.getRequestURI()); // The requested URI

        new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
    }
}