package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.dto.response.DashboardResponse;
import com.example.CoolShopWebsite.model.enums.OrderStatus;
import com.example.CoolShopWebsite.model.enums.ProductStatus;
import com.example.CoolShopWebsite.repository.OrderRepository;
import com.example.CoolShopWebsite.repository.ProductRepository;
import com.example.CoolShopWebsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public DashboardResponse getDashboard() {
        long totalStock    = productRepository.sumStockByStatus(ProductStatus.ACTIVE);
        long totalProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        long newUsers      = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30));
        long totalOrders   = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);

        return DashboardResponse.builder()
                .totalStock(totalStock)
                .totalProducts(totalProducts)
                .newUsers(newUsers)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .build();
    }
}
