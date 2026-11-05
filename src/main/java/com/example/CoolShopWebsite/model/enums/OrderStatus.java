package com.example.CoolShopWebsite.model.enums;

/**
 * Vòng đời trạng thái đơn hàng:
 * PENDING   → CONFIRMED → SHIPPING → COMPLETED
 *                    └──────────────→ CANCELLED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPING,
    COMPLETED,
    CANCELLED
}
