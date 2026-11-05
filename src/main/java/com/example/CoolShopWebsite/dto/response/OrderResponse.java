package com.example.CoolShopWebsite.dto.response;

import com.example.CoolShopWebsite.model.Order;
import com.example.CoolShopWebsite.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String note;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order o) {
        List<OrderItemResponse> items = o.getOrderItems().stream()
                .map(OrderItemResponse::from).toList();
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUser().getId())
                .userEmail(o.getUser().getEmail())
                .totalAmount(o.getTotalAmount())
                .receiverName(o.getReceiverName())
                .receiverPhone(o.getReceiverPhone())
                .shippingAddress(o.getShippingAddress())
                .note(o.getNote())
                .status(o.getStatus())
                .items(items)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
