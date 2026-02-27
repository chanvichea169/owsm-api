package com.example.newsService.service.serviceImpl;

import com.example.newsService.dto.MediaAssetResponse;
import com.example.newsService.dto.request.MediaAssetRequest;
import com.example.newsService.service.MediaAssetService;
import com.example.newsService.service.handler.MediaAssetServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public void delete(Long id) {
        handler.delete(id);
    }
}