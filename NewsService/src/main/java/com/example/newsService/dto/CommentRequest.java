package com.example.newsService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "News ID is required")
    private Long newsId;

    @NotBlank(message = "User name is required")
    private String userName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String userEmail;

    @NotBlank(message = "Comment content is required")
    private String content;
}