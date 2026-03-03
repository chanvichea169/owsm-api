package com.example.newsService.service.serviceImpl;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import com.example.newsService.service.MediaAssetService;
import com.example.newsService.service.handler.MediaAssetServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetServiceHandler handler;

    @Override
    public MediaAssetResponse create(MediaAssetRequest request) {
        return handler.create(request);
    }

    @Override
    public MediaAssetResponse update(Long id, MediaAssetRequest request) {
        return handler.update(id, request);
    }

    @Override
    public MediaAssetResponse getById(Long id) {
        return handler.getById(id);
    }

    @Override
    public List<MediaAssetResponse> getByNews(Long newsId) {
        return handler.getByNews(newsId);
    }

    @Override
    public List<MediaAssetResponse> getByCategory(String category) {
        return handler.getByCategory(category);
    }

    @Override
    public List<MediaAssetResponse> getByNewsAndCategory(Long newsId, String category) {
        return handler.getByNewsAndCategory(newsId, category);
    }

    @Override
    public MediaAssetResponse upload(Long newsId, String category, MultipartFile file) {
        return handler.upload(newsId, category, file);
    }

    @Override
    public void delete(Long id) {
        handler.delete(id);
    }
}
