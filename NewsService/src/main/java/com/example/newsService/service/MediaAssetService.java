package com.example.newsService.service;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;

import java.util.List;


public interface MediaAssetService {

    MediaAssetResponse create(MediaAssetRequest request);

    MediaAssetResponse update(Long id, MediaAssetRequest request);

    MediaAssetResponse getById(Long id);

    List<MediaAssetResponse> getByNews(Long newsId);

    void delete(Long id);
}