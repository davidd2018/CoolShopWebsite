package com.example.CoolShopWebsite.repository;

import com.example.CoolShopWebsite.model.Product;
import com.example.CoolShopWebsite.model.enums.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /** Pessimistic lock – dùng khi trừ tồn kho để tránh oversell */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(Long id);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM Product p WHERE p.status = :status")
    long sumStockByStatus(ProductStatus status);

    long countByStatus(ProductStatus status);
}
