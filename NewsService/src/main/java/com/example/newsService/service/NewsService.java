package com.example.newsService.service;
import com.example.newsService.dto.NewsRequest;
import com.example.newsService.dto.NewsResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NewsService {

    NewsResponse create(NewsRequest request);

    NewsResponse publish(Long id);

    NewsResponse getById(Long id);

    List<NewsResponse> getAll();

    void delete(Long id);
}