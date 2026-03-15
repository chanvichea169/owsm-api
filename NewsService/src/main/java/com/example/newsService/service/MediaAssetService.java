package com.example.newsService.service;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface MediaAssetService {

    MediaAssetResponse create(MediaAssetRequest request);

    MediaAssetResponse update(Long id, MediaAssetRequest request);

    MediaAssetResponse getById(Long id);

    List<MediaAssetResponse> getByNews(Long newsId);
    List<MediaAssetResponse> getByCategory(String category);
    List<MediaAssetResponse> getByNewsAndCategory(Long newsId, String category);
    MediaAssetResponse upload(Long newsId, String category, MultipartFile file);
    MediaAssetResponse uploadNewsImage(Long newsId, String category, MultipartFile file);
    List<MediaAssetResponse> uploadPhotos(Long newsId, String category, List<MultipartFile> files);

    void delete(Long id);
}
