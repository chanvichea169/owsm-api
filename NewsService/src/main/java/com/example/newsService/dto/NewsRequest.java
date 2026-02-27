package com.example.newsService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class NewsRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String category;

    private String coverImage;
    private String author;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}