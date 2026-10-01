package com.example.newsService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String slug;
    private String description;

    private Long parentId;
    private String parentName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}