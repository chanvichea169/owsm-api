package com.example.newsService.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAssetResponse {

    private Long id;
    private Long newsId;

    private String fileUrl;
    private String fileType;
    private Long fileSize;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}