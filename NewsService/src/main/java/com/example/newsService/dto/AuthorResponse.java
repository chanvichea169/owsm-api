package com.example.newsService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthorResponse {

    private Long id;
    private String fullName;
    private String email;
    private String bio;
    private String avatarUrl;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}