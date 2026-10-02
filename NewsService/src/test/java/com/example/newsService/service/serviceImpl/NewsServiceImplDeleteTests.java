package com.example.newsService.service.serviceImpl;

import com.example.newsService.repository.AuthorRepository;
import com.example.newsService.repository.CategoryRepository;
import com.example.newsService.repository.CommentRepository;
import com.example.newsService.repository.MediaAssetRepository;
import com.example.newsService.repository.NewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplDeleteTests {

    @Mock private NewsRepository repository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private MediaAssetRepository mediaAssetRepository;

    @InjectMocks private NewsServiceImpl service;

    @Test
    void clearsDependentsBeforeDeletingNews() {
        when(repository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(commentRepository).deleteAllByNewsId(5L);
        verify(mediaAssetRepository).deleteAllByNewsId(5L);
        verify(repository).deleteNewsTags(5L);
        verify(repository).deleteNewsImages(5L);
        verify(repository).deleteNewsRow(5L);
    }
}
