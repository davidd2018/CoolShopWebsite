package com.example.CoolShopWebsite.model.enums;

/**
 * Trạng thái sản phẩm.
 * ACTIVE       – đang bán, hiển thị cho customer
 * INACTIVE     – ngừng kinh doanh, ẩn khỏi customer
 * OUT_OF_STOCK – hết hàng (có thể dùng thay vì stock_quantity == 0)
 */
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK
}
