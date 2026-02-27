package com.hms.GatewayOWSM.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/users/**", "/api/roles/**")
                        .uri("http://localhost:8081")
                )
                .route("news-service", r -> r
                        .path("/api/news/**", "/api/comments/**", "/api/media-assets/**", "/api/categories/**", "/api/tags/**", "/api/authors/**")
                        .uri("http://localhost:8082")
                )
                .build();
    }
}
