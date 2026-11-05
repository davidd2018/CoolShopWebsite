package com.example.CoolShopWebsite.model.enums;

/**
 * Trạng thái thanh toán, độc lập với OrderStatus.
 * PENDING  – chưa thanh toán (COD mặc định)
 * PAID     – đã thanh toán thành công
 * FAILED   – thanh toán thất bại
 * REFUNDED – đã hoàn tiền
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
