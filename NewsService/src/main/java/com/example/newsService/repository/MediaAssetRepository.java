package com.example.newsService.repository;

import com.example.newsService.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findByNewsId(Long newsId);
    List<MediaAsset> findByCategoryIgnoreCase(String category);
    List<MediaAsset> findByNewsIdAndCategoryIgnoreCase(Long newsId, String category);
}
