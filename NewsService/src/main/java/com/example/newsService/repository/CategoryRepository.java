package com.example.newsService.repository;

import com.example.newsService.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Override
    Optional<Category> findById(Long aLong);
}
