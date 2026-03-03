package com.example.newsService.controller;

import com.example.newsService.dto.ApiResponse;
import com.example.newsService.dto.NewsRequest;
import com.example.newsService.dto.NewsResponse;
import jakarta.validation.Valid;
import com.example.newsService.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/news", "/api/news"})
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @PostMapping
    public ResponseEntity<ApiResponse<NewsResponse>> create(@Valid @RequestBody NewsRequest request) {
        NewsResponse response = newsService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse("News created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(buildResponse("News fetched successfully", newsService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> all() {
        return ResponseEntity.ok(buildResponse("News list fetched successfully", newsService.getAll()));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<NewsResponse>> publish(@PathVariable Long id) {
        return ResponseEntity.ok(buildResponse("News published successfully", newsService.publish(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsResponse>> update(@PathVariable Long id, @Valid @RequestBody NewsRequest request) {
        return ResponseEntity.ok(buildResponse("News updated successfully", newsService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private <T> ApiResponse<T> buildResponse(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
