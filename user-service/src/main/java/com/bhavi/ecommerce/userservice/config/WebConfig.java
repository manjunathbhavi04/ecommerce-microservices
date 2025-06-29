package com.bhavi.ecommerce.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Marks this as a Spring configuration class
public class WebConfig implements WebMvcConfigurer { // Implements WebMvcConfigurer for more customization options

    @Bean
    public CorsFilter corsFilter() { // This bean will be auto-detected by Spring Security
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = getCorsConfiguration();
        config.setAllowCredentials(true); // Allows credentials (like cookies or Authorization headers) to be sent

        source.registerCorsConfiguration("/**", config); // Apply this CORS configuration to all paths
        return new CorsFilter(source);
    }

    private static CorsConfiguration getCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();

        // --- IMPORTANT: Configure your allowed origins ---
        config.addAllowedOrigin("http://localhost:5173"); // Your React frontend development server
        // For production, you would add your actual production domain(s) here:
        // config.addAllowedOrigin("https://your-production-frontend.com");
        // config.addAllowedOrigin("https://another-allowed-domain.com");

        config.addAllowedHeader("*"); // Allows all headers
        config.addAllowedMethod("*"); // Allows all HTTP methods (GET, POST, PUT, DELETE, OPTIONS etc.)
        return config;
    }

    // You can add other web configurations here if needed, e.g.:
    // @Override
    // public void addFormatters(FormatterRegistry registry) { ... }
    // @Override
    // public void addResourceHandlers(ResourceHandlerRegistry registry) { ... }
}