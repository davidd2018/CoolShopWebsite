package com.example.CoolShopWebsite.model;

import com.example.CoolShopWebsite.model.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sản phẩm bán hàng, bao gồm thông tin tồn kho.
 * Maps tới bảng: products
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_status",      columnList = "status"),
        @Index(name = "idx_products_name",        columnList = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → categories.id */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Giá bán hiện tại. Dùng BigDecimal để tránh lỗi làm tròn.
     * Giá tại thời điểm đặt hàng được lưu trong OrderItem.unitPrice.
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    @Digits(integer = 13, fraction = 2)
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Số lượng tồn kho hiện tại.
     * Service layer phải dùng SELECT...FOR UPDATE khi trừ tồn kho
     * để tránh race condition.
     */
    @NotNull
    @Min(value = 0, message = "Tồn kho không được âm")
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20,
            columnDefinition = "ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') DEFAULT 'ACTIVE'")
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Relationships
    // ----------------------------------------------------------------

    /** Một Product xuất hiện trong nhiều OrderItems. */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
