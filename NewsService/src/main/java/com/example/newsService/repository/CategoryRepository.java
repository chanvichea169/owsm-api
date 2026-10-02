package com.example.newsService.repository;

import com.example.newsService.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Override
    Optional<Category> findById(Long aLong);

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findBySlugIgnoreCase(String slug);

    /** Keeps sub-categories but clears their parent link before deletion. */
    @Modifying
    @Query("update Category category set category.parent = null where category.parent.id = :categoryId")
    void detachChildren(@Param("categoryId") Long categoryId);
}
