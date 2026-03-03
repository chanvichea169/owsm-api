package com.example.newsService.service.serviceImpl;

import com.example.newsService.dto.*;
import com.example.newsService.enumeration.NewsStatus;
import com.example.newsService.exception.ResourceNotFoundException;
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
        News news = News.builder()
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .content(request.getContent())
                .coverImage(request.getCoverImage())
                .isFeatured(request.getIsFeatured())
                .viewCount(0L)
                .status(NewsStatus.DRAFT.name())
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
                    news.setCoverImage(request.getCoverImage());
                    news.setIsFeatured(request.getIsFeatured());
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
                .coverImage(news.getCoverImage())
                .status(news.getStatus())
                .isFeatured(news.getIsFeatured())
                .viewCount(news.getViewCount())
                .publishedAt(news.getPublishedAt())
                .createdAt(news.getCreatedAt())
                .build();
    }
}
