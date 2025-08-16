package com.bhavi.ecommerce.orderservice.config;

import com.bhavi.ecommerce.orderservice.security.jwt.CustomAuthenticationEntryPoint;
import com.bhavi.ecommerce.orderservice.security.jwt.JwtAuthenticationFilter;
import com.bhavi.ecommerce.orderservice.security.jwt.JwtUtil;
import com.bhavi.ecommerce.orderservice.service.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtUtil jwtUtil;

    // Define ProductServiceClient as a Bean here
//    @Bean
//    public ProductServiceClient productServiceClient(WebClient.Builder webClientBuilder,
//                                                     @Value("${product.service.url}") String productServiceBaseUrl) {
//        return new ProductServiceClient(webClientBuilder, productServiceBaseUrl);
//    }

    @Bean
    // This method is invoked when ever there a request from the order api
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // to disable the html code while testing in postman
                .formLogin(AbstractHttpConfigurer::disable) // to disable the spring security login form to appear when login
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // to detect unauthorized users
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/orders/**").authenticated()
                                .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
