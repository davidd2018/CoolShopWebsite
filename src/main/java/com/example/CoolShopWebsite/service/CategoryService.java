package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.dto.request.CategoryRequest;
import com.example.CoolShopWebsite.dto.response.CategoryResponse;
import com.example.CoolShopWebsite.exception.AppException;
import com.example.CoolShopWebsite.model.Category;
import com.example.CoolShopWebsite.model.enums.CategoryStatus;
import com.example.CoolShopWebsite.repository.CategoryRepository;
import com.example.CoolShopWebsite.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    public CategoryResponse create(CategoryRequest req) {
        Category category = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest req) {
        Category category = getOrThrow(id);
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse updateStatus(Long id, CategoryStatus status) {
        Category category = getOrThrow(id);
        category.setStatus(status);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        getOrThrow(id); // xác nhận tồn tại
        if (productRepository.existsByCategoryId(id)) {
            throw AppException.conflict("Cannot delete category with linked products");
        }
        categoryRepository.deleteById(id);
    }

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Category not found: " + id));
    }
}
