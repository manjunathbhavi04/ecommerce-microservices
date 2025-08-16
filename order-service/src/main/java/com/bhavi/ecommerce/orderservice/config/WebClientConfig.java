package com.bhavi.ecommerce.orderservice.config;

import com.bhavi.ecommerce.orderservice.service.client.ProductServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientConfig {

    // Removed @Value("${user.service.url}") String userServiceUrl; as it's not used directly here anymore.
    // ServiceTokenProvider uses it internally.

    private final WebClient.Builder defaultWebClientBuilder;
    private final ServiceTokenProvider serviceTokenProvider; // Inject the ServiceTokenProvider

    // Constructor injection
    public WebClientConfig(WebClient.Builder defaultWebClientBuilder,
                           ServiceTokenProvider serviceTokenProvider) {
        this.defaultWebClientBuilder = defaultWebClientBuilder;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    // This bean is GONE. ServiceTokenProvider now creates its own internal WebClient for login.
    // If you need a completely unauthenticated WebClient (e.g., for public APIs), you can define one.
    // @Bean
    // public WebClient unauthenticatedWebClient() {
    //     return defaultWebClientBuilder.build();
    // }

    private ExchangeFilterFunction jwtAuthenticationFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String serviceJwtToken = serviceTokenProvider.getServiceJwtToken();

            if (serviceJwtToken != null && !serviceJwtToken.isEmpty()) {
                return Mono.just(ClientRequest.from(clientRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceJwtToken)
                        .build());
            }
            log.warn("No JWT token available. Proceeding with request without Authorization header: {}", clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    @Bean
    public ProductServiceClient productServiceClient(@Value("${product.service.url}") String productServiceBaseUrl) {
        WebClient authenticatedWebClient = defaultWebClientBuilder // Still use the global builder
                .baseUrl(productServiceBaseUrl)
                .filter(jwtAuthenticationFilter()) // Apply the JWT filter
                .build();
        log.info("ProductServiceClient bean (configured for authenticated inter-service calls to {}) created.", productServiceBaseUrl);
        return new ProductServiceClient(authenticatedWebClient);
    }
}