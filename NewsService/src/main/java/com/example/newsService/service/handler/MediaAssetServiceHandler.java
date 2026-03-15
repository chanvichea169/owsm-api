package com.example.newsService.service.handler;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import com.example.newsService.exception.ResourceNotFoundException;
import com.example.newsService.model.MediaAsset;
import com.example.newsService.model.News;
import com.example.newsService.repository.MediaAssetRepository;
import com.example.newsService.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MediaAssetServiceHandler {

    private final MediaAssetRepository mediaAssetRepository;
    private final NewsRepository newsRepository;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "csv", "ppt", "pptx",
            "jpg", "jpeg", "png", "gif", "webp"
    );

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.news-dir:upload/news}")
    private String newsUploadDir;

    public MediaAssetResponse create(MediaAssetRequest request) {
        News news = newsRepository.findById(request.getNewsId())
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));

        MediaAsset media = MediaAsset.builder()
                .news(news)
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .category(request.getCategory())
                .build();

        mediaAssetRepository.save(media);
        return mapToResponse(media);
    }

    public MediaAssetResponse update(Long id, MediaAssetRequest request) {
        MediaAsset media = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found"));

        media.setFileUrl(request.getFileUrl());
        media.setFileType(request.getFileType());
        media.setFileSize(request.getFileSize());
        media.setCategory(request.getCategory());

        mediaAssetRepository.save(media);
        return mapToResponse(media);
    }

    public MediaAssetResponse getById(Long id) {
        MediaAsset media = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found"));
        return mapToResponse(media);
    }

    public List<MediaAssetResponse> getByNews(Long newsId) {
        return mediaAssetRepository.findByNewsId(newsId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaAssetResponse> getByCategory(String category) {
        return mediaAssetRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaAssetResponse> getByNewsAndCategory(Long newsId, String category) {
        return mediaAssetRepository.findByNewsIdAndCategoryIgnoreCase(newsId, category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MediaAssetResponse upload(Long newsId, String category, MultipartFile file) {
        News news = resolveNews(newsId);
        return storeMediaAsset(news, category, file, Paths.get(uploadDir));
    }

    public MediaAssetResponse uploadNewsImage(Long newsId, String category, MultipartFile file) {
        News news = resolveNews(newsId);
        return storeMediaAsset(news, category, file, Paths.get(newsUploadDir));
    }

    public List<MediaAssetResponse> uploadPhotos(Long newsId, String category, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one photo is required");
        }
        News news = resolveNews(newsId);
        Path uploadPath = Paths.get(uploadDir);
        return files.stream()
                .map(file -> storeMediaAsset(news, category, file, uploadPath))
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        if (!mediaAssetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Media asset not found");
        }
        mediaAssetRepository.deleteById(id);
    }

    private MediaAssetResponse storeMediaAsset(News news, String category, MultipartFile file, Path directory) {
        validateCategory(category);
        validateFile(file);
        String originalName = Paths.get(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName()
                .toString();
        String extension = getExtension(originalName);
        validateExtension(extension);

        String storedName = UUID.randomUUID() + "_" + originalName;
        Path target = directory.resolve(storedName);

        try {
            Files.createDirectories(directory);
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        MediaAsset media = MediaAsset.builder()
                .news(news)
                .fileUrl(target.toString())
                .fileType(file.getContentType() != null ? file.getContentType() : extension)
                .fileSize(file.getSize())
                .category(category)
                .originalFileName(originalName)
                .storedFileName(storedName)
                .build();

        mediaAssetRepository.save(media);
        return mapToResponse(media);
    }

    private News resolveNews(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
    }

    private void validateExtension(String extension) {
        if (extension.isBlank() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    private MediaAssetResponse mapToResponse(MediaAsset media) {
        return MediaAssetResponse.builder()
                .id(media.getId())
                .newsId(media.getNews().getId())
                .fileUrl(media.getFileUrl())
                .fileType(media.getFileType())
                .fileSize(media.getFileSize())
                .category(media.getCategory())
                .originalFileName(media.getOriginalFileName())
                .storedFileName(media.getStoredFileName())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
}
