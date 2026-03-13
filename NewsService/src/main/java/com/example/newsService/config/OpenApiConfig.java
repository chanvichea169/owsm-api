package com.example.newsService.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "NewsService API",
                version = "1.0",
                description = "REST APIs for managing news and media assets"
        ),
        servers = {
                @Server(url = "/", description = "Local host")
        }
)
@Configuration
public class OpenApiConfig {
}
