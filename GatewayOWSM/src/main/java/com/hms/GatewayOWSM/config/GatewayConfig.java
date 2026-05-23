package com.hms.GatewayOWSM.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${auth.service.uri}")
    private String authServiceUri;

    @Value("${news.service.uri}")
    private String newsServiceUri;

    @Value("${attendance.service.uri}")
    private String attendanceServiceUri;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/users/**", "/api/roles/**", "/api/profile/**", "/uploads/profiles/**")
                        .uri(authServiceUri)
                )
                .route("news-service", r -> r
                        .path("/api/news/**", "/api/comments/**", "/api/media-assets/**", "/api/categories/**", "/api/tags/**", "/api/authors/**", "/uploads/news/**")
                        .uri(newsServiceUri)
                )
                .route("attendance-service", r -> r
                        .path("/api/attendance/**", "/api/companies/**", "/api/employees/**", "/api/companies/*/offices/**", "/uploads/**")
                        .uri(attendanceServiceUri)
                )
                .build();
    }
}
