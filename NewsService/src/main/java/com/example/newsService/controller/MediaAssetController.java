package com.example.newsService.controller;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import com.example.newsService.service.MediaAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media-assets")
@RequiredArgsConstructor
public class MediaAssetController {

    private final MediaAssetService mediaAssetService;

    @PostMapping
    public MediaAssetResponse create(@Valid @RequestBody MediaAssetRequest request) {
        return mediaAssetService.create(request);
    }

    @PutMapping("/{id}")
    public MediaAssetResponse update(@PathVariable Long id,
                                     @Valid @RequestBody MediaAssetRequest request) {
        return mediaAssetService.update(id, request);
    }

    @GetMapping("/{id}")
    public MediaAssetResponse getById(@PathVariable Long id) {
        return mediaAssetService.getById(id);
    }

    @GetMapping("/news/{newsId}")
    public List<MediaAssetResponse> getByNews(@PathVariable Long newsId) {
        return mediaAssetService.getByNews(newsId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mediaAssetService.delete(id);
    }
}