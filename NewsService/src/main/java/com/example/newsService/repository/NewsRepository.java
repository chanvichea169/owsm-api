package com.example.newsService.repository;

import com.example.newsService.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    Optional<News> findBySlug(String slug);

    /* The join table and element collection behind an article have no cascade
       rule, so they are cleared explicitly before the article is removed. */

    @Modifying
    @Query(value = "delete from tbl_news_tags where news_id = :newsId", nativeQuery = true)
    void deleteNewsTags(@Param("newsId") Long newsId);

    @Modifying
    @Query(value = "delete from tbl_news_images where news_id = :newsId", nativeQuery = true)
    void deleteNewsImages(@Param("newsId") Long newsId);

    @Modifying
    @Query(value = "delete from tbl_news where id = :newsId", nativeQuery = true)
    void deleteNewsRow(@Param("newsId") Long newsId);

    @Modifying
    @Query("update News news set news.category = null where news.category.id = :categoryId")
    void detachCategory(@Param("categoryId") Long categoryId);
}