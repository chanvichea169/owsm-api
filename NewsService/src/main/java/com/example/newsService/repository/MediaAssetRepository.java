package com.example.newsService.repository;

import com.example.newsService.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}
