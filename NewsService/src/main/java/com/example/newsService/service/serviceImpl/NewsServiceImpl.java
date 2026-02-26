package com.example.newsService.service.serviceImpl;

import com.example.newsService.dto.*;
import com.example.newsService.enumeration.NewsStatus;
import com.example.newsService.model.*;
import com.example.newsService.repository.NewsRepository;
import com.example.newsService.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository repository;

    @Override
    public NewsResponse create(NewsRequest request) {
        return null;
    }

    @Override
    public NewsResponse publish(Long id) {
        News news = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));

        news.setStatus(NewsStatus.PUBLISHED);
        news.setPublishedAt(LocalDateTime.now());

        repository.save(news);

        return map(news);
    }

    @Override
    public NewsResponse getById(Long id) {
        return map(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found")));
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
        repository.deleteById(id);
    }

    private String generateSlug(String title){
        return title.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9-]", "");
    }

    private NewsResponse map(News news){
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .slug(news.getSlug())
                .content(news.getContent())
                .category(news.getCategory())
                .coverImage(news.getCoverImage())
                .author(news.getAuthor())
                .status(news.getStatus().name())
                .isFeatured(news.getIsFeatured())
                .viewCount(news.getViewCount())
                .publishedAt(news.getPublishedAt())
                .createdAt(news.getCreatedAt())
                .build();
    }
}