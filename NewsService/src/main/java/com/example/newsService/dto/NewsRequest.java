package com.example.newsService.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

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
}