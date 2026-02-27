package com.example.newsService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAssetRequest {

    @NotNull(message = "News ID is required")
    private Long newsId;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotBlank(message = "File type is required")
    private String fileType;

    private Long fileSize;
}