package com.example.CoolShopWebsite.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {
    private Long totalStock;
    private Long totalProducts;
    private Long newUsers;
    private Long totalOrders;
    private Long pendingOrders;
}
