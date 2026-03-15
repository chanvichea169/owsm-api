package com.example.newsService.service;
import com.example.newsService.dto.NewsRequest;
import com.example.newsService.dto.NewsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface NewsService {

    NewsResponse create(NewsRequest request);

    NewsResponse publish(Long id);

    NewsResponse update(Long id, NewsRequest request);

    NewsResponse getById(Long id);

    List<NewsResponse> getAll();

    void delete(Long id);

    String uploadFile(MultipartFile file);
}