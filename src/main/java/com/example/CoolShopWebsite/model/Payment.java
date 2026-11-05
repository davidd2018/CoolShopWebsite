package com.example.CoolShopWebsite.model;

import com.example.CoolShopWebsite.model.enums.PaymentMethod;
import com.example.CoolShopWebsite.model.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thông tin thanh toán của đơn hàng.
 * Maps tới bảng: payments
 *
 * MVP: 1 Order – 1 Payment (UNIQUE constraint trên order_id).
 * payment_status độc lập với order_status.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_status", columnList = "payment_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK → orders.id, UNIQUE: mỗi đơn chỉ có 1 payment.
     * @OneToOne sẽ tự tạo UNIQUE constraint khi dùng @JoinColumn.
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20,
            columnDefinition = "ENUM('COD') DEFAULT 'COD'")
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    /**
     * Phải khớp với order.totalAmount.
     * Service validate trước khi lưu.
     */
    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 13, fraction = 2)
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20,
            columnDefinition = "ENUM('PENDING','PAID','FAILED','REFUNDED') DEFAULT 'PENDING'")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
