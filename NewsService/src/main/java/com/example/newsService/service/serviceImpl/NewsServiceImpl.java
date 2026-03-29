package com.example.newsService.service.serviceImpl;

import com.example.newsService.dto.*;
import com.example.newsService.enumeration.NewsStatus;
import com.example.newsService.exception.ResourceNotFoundException;
import com.example.newsService.model.*;
import com.example.newsService.repository.AuthorRepository;
import com.example.newsService.repository.CategoryRepository;
import com.example.newsService.repository.NewsRepository;
import com.example.newsService.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository repository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.file.news-dir:uploads/news}")
    private String newsUploadDir;

    @Value("${app.file.gallery-dir:uploads/news/gallary_images}")
    private String galleryUploadDir;

    @Override
    public NewsResponse create(NewsRequest request) {
        News news = News.builder()
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .content(request.getContent())
                .category(resolveCategory(request.getCategory()))
                .author(resolveAuthor(request.getAuthor()))
                .coverImage(normalizeCoverImage(request.getCoverImage()))
                .images(request.getImages() != null ? normalizeImageList(request.getImages()) : new ArrayList<>())
                .isFeatured(request.getIsFeatured())
                .viewCount(0L)
                .status(NewsStatus.DRAFT.name())
                .publishedAt(request.getPublishedAt())
                .build();

        return map(repository.save(news));
    }

    @Override
    public NewsResponse publish(Long id) {
        News news = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));
        news.setStatus(NewsStatus.PUBLISHED.name());
        news.setPublishedAt(LocalDateTime.now());

        repository.save(news);

        return map(news);
    }

    @Override
    public NewsResponse update(Long id, NewsRequest request) {
        return repository.findById(id)
                .map(news -> {
                    news.setTitle(request.getTitle());
                    news.setSlug(generateSlug(request.getTitle()));
                    news.setContent(request.getContent());
                    news.setCategory(resolveCategory(request.getCategory()));
                    news.setAuthor(resolveAuthor(request.getAuthor()));
                    news.setCoverImage(normalizeCoverImage(request.getCoverImage()));
                    if (request.getImages() != null) {
                        news.setImages(normalizeImageList(request.getImages()));
                    }
                    news.setIsFeatured(request.getIsFeatured());
                    news.setPublishedAt(request.getPublishedAt());
                    return map(repository.save(news));
                })
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));
    }

    @Override
    public NewsResponse getById(Long id) {
        return map(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found")));
    }

    @Override
    public List<NewsResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("News not found");
        }
        repository.deleteById(id);
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalName = Paths.get(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName()
                .toString();
        String extension = getExtension(originalName);
        if (extension.isBlank() || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image type: " + extension);
        }

        String storedName = UUID.randomUUID() + "_" + originalName;
        Path directory = Paths.get(newsUploadDir);
        Path target = directory.resolve(storedName);

        try {
            Files.createDirectories(directory);
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }

        return storedName;
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                uploadedFiles.add(uploadGalleryFile(file));
            }
        }
        return uploadedFiles;
    }

    private String uploadGalleryFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalName = Paths.get(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName()
                .toString();
        String extension = getExtension(originalName);
        if (extension.isBlank() || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image type: " + extension);
        }

        String storedName = UUID.randomUUID() + "_" + originalName;
        Path directory = Paths.get(galleryUploadDir);
        Path target = directory.resolve(storedName);

        try {
            Files.createDirectories(directory);
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store gallery image", e);
        }

        return "news/gallary_images/" + storedName;
    }

    private String generateSlug(String title){
        return title.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9-]", "");
    }

    private String normalizeCoverImage(String coverImage) {
        if (coverImage == null || coverImage.isBlank()) {
            return coverImage;
        }
        String normalized = coverImage.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        return slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
    }

    private List<String> normalizeImageList(List<String> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        List<String> normalized = images.stream()
                .filter(image -> image != null && !image.isBlank())
                .map(this::normalizeGalleryImage)
                .toList();
        return new ArrayList<>(normalized);
    }

    private String normalizeGalleryImage(String image) {
        String normalized = image.replace('\\', '/').replaceFirst("^/+", "");
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        if (normalized.startsWith("news/")) {
            return normalized;
        }
        if (normalized.startsWith("gallary_images/")) {
            return "news/" + normalized;
        }
        if (!normalized.contains("/")) {
            return "news/gallary_images/" + normalized;
        }
        return normalized;
    }

    private Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        return categoryRepository.findByNameIgnoreCase(categoryName.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
    }

    private Author resolveAuthor(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            return null;
        }
        return authorRepository.findByFullNameIgnoreCase(authorName.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorName));
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    private NewsResponse map(News news){
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .slug(news.getSlug())
                .content(news.getContent())
                .category(news.getCategory() != null ? news.getCategory().getName() : null)
                .coverImage(news.getCoverImage())
                .images(news.getImages())
                .author(news.getAuthor() != null ? news.getAuthor().getFullName() : null)
                .status(news.getStatus())
                .isFeatured(news.getIsFeatured())
                .viewCount(news.getViewCount())
                .publishedAt(news.getPublishedAt())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }
}
