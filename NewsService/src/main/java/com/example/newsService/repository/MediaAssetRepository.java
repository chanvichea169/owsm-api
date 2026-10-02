package com.example.newsService.repository;

import com.example.newsService.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findByNewsId(Long newsId);
    List<MediaAsset> findByCategoryIgnoreCase(String category);
    List<MediaAsset> findByNewsIdAndCategoryIgnoreCase(Long newsId, String category);

    @Modifying
    @Query("delete from MediaAsset asset where asset.news.id = :newsId")
    void deleteAllByNewsId(@Param("newsId") Long newsId);
}
