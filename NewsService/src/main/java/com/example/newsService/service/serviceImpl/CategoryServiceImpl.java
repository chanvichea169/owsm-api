package com.example.newsService.service.serviceImpl;

import com.example.newsService.dto.CategoryRequest;
import com.example.newsService.dto.CategoryResponse;
import com.example.newsService.exception.ResourceNotFoundException;
import com.example.newsService.model.Category;
import com.example.newsService.repository.CategoryRepository;
import com.example.newsService.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .parent(resolveParent(request.getParentId()))
                .build();
        return map(repository.save(category));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        return repository.findById(id)
                .map(category -> {
                    category.setName(request.getName());
                    category.setSlug(generateSlug(request.getName()));
                    category.setDescription(request.getDescription());
                    category.setParent(resolveParent(request.getParentId()));
                    return map(repository.save(category));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    public CategoryResponse getById(Long id) {
        return map(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
    }

    @Override
    public List<CategoryResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        repository.deleteById(id);
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return repository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .trim()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9-]", "");
    }

    private CategoryResponse map(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
