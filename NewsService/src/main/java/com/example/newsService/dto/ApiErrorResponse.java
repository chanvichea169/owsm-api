package com.example.newsService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private int status;
    private String path;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
}
