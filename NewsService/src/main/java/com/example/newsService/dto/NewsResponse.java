package com.example.newsService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewsResponse {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String category;
    private String coverImage;
    private String author;
    private String status;
    private Boolean isFeatured;
    private Long viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}