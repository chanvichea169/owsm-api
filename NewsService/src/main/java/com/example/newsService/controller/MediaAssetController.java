package com.example.newsService.controller;

import com.example.newsService.dto.ApiResponse;
import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import com.example.newsService.service.MediaAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/media-assets", "/api/media-assets"})
@RequiredArgsConstructor
public class MediaAssetController {

    private final MediaAssetService mediaAssetService;

    @PostMapping
    public ResponseEntity<ApiResponse<MediaAssetResponse>> create(@Valid @RequestBody MediaAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildResponse("Media asset created successfully", mediaAssetService.create(request)));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<MediaAssetResponse>> upload(@RequestParam Long newsId,
                                                                  @RequestParam String category,
                                                                  @RequestPart MultipartFile file) {
        MediaAssetResponse response = mediaAssetService.upload(newsId, category, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildResponse("Media asset uploaded successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaAssetResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody MediaAssetRequest request) {
        return ResponseEntity.ok(buildResponse("Media asset updated successfully", mediaAssetService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaAssetResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(buildResponse("Media asset fetched successfully", mediaAssetService.getById(id)));
    }

    @GetMapping(params = {"newsId", "category"})
    public ResponseEntity<ApiResponse<List<MediaAssetResponse>>> getByNewsAndCategory(@RequestParam Long newsId,
                                                                                       @RequestParam String category) {
        return ResponseEntity.ok(buildResponse("Media asset list fetched successfully",
                mediaAssetService.getByNewsAndCategory(newsId, category)));
    }

    @GetMapping(params = "newsId")
    public ResponseEntity<ApiResponse<List<MediaAssetResponse>>> getByNewsId(@RequestParam Long newsId) {
        return ResponseEntity.ok(buildResponse("Media asset list fetched successfully", mediaAssetService.getByNews(newsId)));
    }

    @GetMapping(params = "category")
    public ResponseEntity<ApiResponse<List<MediaAssetResponse>>> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(buildResponse("Media asset list fetched successfully", mediaAssetService.getByCategory(category)));
    }

    @GetMapping("/news/{newsId}")
    public ResponseEntity<ApiResponse<List<MediaAssetResponse>>> getByNewsLegacy(@PathVariable Long newsId) {
        return ResponseEntity.ok(buildResponse("Media asset list fetched successfully", mediaAssetService.getByNews(newsId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaAssetService.delete(id);
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
