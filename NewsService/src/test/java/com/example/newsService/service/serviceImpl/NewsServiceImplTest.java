package com.example.newsService.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.example.newsService.dto.NewsRequest;
import com.example.newsService.model.Category;
import com.example.newsService.model.News;
import com.example.newsService.repository.AuthorRepository;
import com.example.newsService.repository.CategoryRepository;
import com.example.newsService.repository.NewsRepository;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private NewsServiceImpl service;

    @Test
    void getAllReturnsNewsResponsesSerializableForRedisCache() {
        News news = News.builder()
            .id(1L)
            .title("News")
            .images(List.of("news/image.jpg"))
            .build();
        when(repository.findAll()).thenReturn(List.of(news));

        List<?> response = service.getAll();

        assertDoesNotThrow(() -> {
            try (
                ObjectOutputStream output = new ObjectOutputStream(
                    new ByteArrayOutputStream()
                )
            ) {
                output.writeObject(response);
            }
        });
    }

    @Test
    void createResolvesCategorySlugSentByNewsForm() {
        Category category = Category.builder()
            .id(7L)
            .name("Synthetic Seed Category 007")
            .slug("seed100-category-007")
            .build();
        when(categoryRepository.findByNameIgnoreCase("seed100-category-007"))
            .thenReturn(java.util.Optional.empty());
        when(categoryRepository.findBySlugIgnoreCase("seed100-category-007"))
            .thenReturn(java.util.Optional.of(category));
        when(repository.save(any(News.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        NewsRequest request = NewsRequest.builder()
            .title("New article")
            .content("Article content")
            .category("seed100-category-007")
            .build();

        var response = service.create(request);

        assertEquals("Synthetic Seed Category 007", response.getCategory());
        verify(categoryRepository).findBySlugIgnoreCase("seed100-category-007");
    }
}
