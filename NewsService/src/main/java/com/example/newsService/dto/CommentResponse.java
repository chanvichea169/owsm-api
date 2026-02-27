package com.example.newsService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;

    private Long newsId;
    private String newsTitle;

    private String userName;
    private String userEmail;
    private String content;

    private Boolean isApproved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}