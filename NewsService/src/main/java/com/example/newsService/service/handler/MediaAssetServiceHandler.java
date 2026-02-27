package com.example.newsService.service.handler;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import com.example.newsService.model.MediaAsset;
import com.example.newsService.model.News;
import com.example.newsService.repository.MediaAssetRepository;
import com.example.newsService.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MediaAssetServiceHandler {

    private final MediaAssetRepository mediaAssetRepository;
    private final NewsRepository newsRepository;

    public MediaAssetResponse create(MediaAssetRequest request) {

        News news = newsRepository.findById(request.getNewsId())
                .orElseThrow(() -> new RuntimeException("News not found"));

        MediaAsset media = MediaAsset.builder()
                .news(news)
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .build();

        mediaAssetRepository.save(media);

        return mapToResponse(media);
    }

    public MediaAssetResponse update(Long id, MediaAssetRequest request) {

        MediaAsset media = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MediaAsset not found"));

        media.setFileUrl(request.getFileUrl());
        media.setFileType(request.getFileType());
        media.setFileSize(request.getFileSize());

        mediaAssetRepository.save(media);

        return mapToResponse(media);
    }

    public MediaAssetResponse getById(Long id) {
        MediaAsset media = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MediaAsset not found"));
        return mapToResponse(media);
    }

    public List<MediaAssetResponse> getByNews(Long newsId) {
        return mediaAssetRepository.findById(newsId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        mediaAssetRepository.deleteById(id);
    }

    private MediaAssetResponse mapToResponse(MediaAsset media) {
        return MediaAssetResponse.builder()
                .id(media.getId())
                .newsId(media.getNews().getId())
                .fileUrl(media.getFileUrl())
                .fileType(media.getFileType())
                .fileSize(media.getFileSize())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
}