package com.hms.GatewayHMS.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("UserHMS", r -> r
                    .path("/api/users/**")
                    .filters(f -> f
                            .rewritePath("/api/users/(?<segment>.*)", "/api/users/${segment}")
                            .addResponseHeader("X-Gateway", "AuthService")
                    )
                    .uri("http://localhost:8081")
            )
            .build();
    }
}
