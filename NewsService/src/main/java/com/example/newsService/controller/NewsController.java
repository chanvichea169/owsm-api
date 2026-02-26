package com.example.newsService.controller;

import com.example.newsService.dto.NewsRequest;
import com.example.newsService.dto.NewsResponse;
import com.example.newsService.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    @PostMapping
    public NewsResponse create(@Validated @RequestBody NewsRequest request){
        return newsService.create(request);
    }

    @GetMapping("/{id}")
    public NewsResponse get(@PathVariable Long id){
        return newsService.getById(id);
    }

    @GetMapping
    public List<NewsResponse> all(){
        return newsService.getAll();
    }
}