package com.example.newsService.repository;

import com.example.newsService.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    Optional<News> findBySlug(String slug);
}