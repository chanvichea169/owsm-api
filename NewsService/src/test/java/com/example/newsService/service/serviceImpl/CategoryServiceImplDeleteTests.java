package com.example.newsService.service.serviceImpl;

import com.example.newsService.repository.CategoryRepository;
import com.example.newsService.repository.NewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplDeleteTests {

    @Mock private CategoryRepository repository;
    @Mock private NewsRepository newsRepository;

    @InjectMocks private CategoryServiceImpl service;

    @Test
    void detachesNewsAndChildrenBeforeDeletingCategory() {
        when(repository.existsById(3L)).thenReturn(true);

        service.delete(3L);

        verify(newsRepository).detachCategory(3L);
        verify(repository).detachChildren(3L);
        verify(repository).deleteById(3L);
    }
}
