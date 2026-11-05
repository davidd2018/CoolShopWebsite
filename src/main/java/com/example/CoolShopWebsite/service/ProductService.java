package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.dto.request.InventoryAdjustRequest;
import com.example.CoolShopWebsite.dto.request.ProductRequest;
import com.example.CoolShopWebsite.dto.response.ProductResponse;
import com.example.CoolShopWebsite.exception.AppException;
import com.example.CoolShopWebsite.model.Category;
import com.example.CoolShopWebsite.model.Product;
import com.example.CoolShopWebsite.model.enums.ProductStatus;
import com.example.CoolShopWebsite.repository.CategoryRepository;
import com.example.CoolShopWebsite.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    public ProductResponse create(ProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> AppException.notFound("Category not found"));
        Product product = Product.builder()
                .category(category)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .imageUrl(req.getImageUrl())
                .stockQuantity(req.getStockQuantity())
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product product = getOrThrow(id);
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> AppException.notFound("Category not found"));
        product.setCategory(category);
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setImageUrl(req.getImageUrl());
        product.setStockQuantity(req.getStockQuantity());
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse updateStatus(Long id, ProductStatus status) {
        Product product = getOrThrow(id);
        product.setStatus(status);
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = getOrThrow(id);
        if (!product.getOrderItems().isEmpty()) {
            throw AppException.conflict("Cannot delete product that has been ordered");
        }
        productRepository.delete(product);
    }

    // --- Inventory ---

    public Page<ProductResponse> inventoryList(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse inventoryDetail(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    @Transactional
    public ProductResponse adjustStock(Long id, InventoryAdjustRequest req) {
        if (req.getAdjustment() == null || req.getAdjustment() == 0) {
            throw AppException.badRequest("Adjustment must not be zero");
        }
        // Dùng pessimistic lock để tránh race condition
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> AppException.notFound("Product not found: " + id));
        int newStock = product.getStockQuantity() + req.getAdjustment();
        if (newStock < 0) {
            throw AppException.badRequest("Insufficient stock. Current: " + product.getStockQuantity());
        }
        product.setStockQuantity(newStock);
        return ProductResponse.from(productRepository.save(product));
    }

    public Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Product not found: " + id));
    }
}
