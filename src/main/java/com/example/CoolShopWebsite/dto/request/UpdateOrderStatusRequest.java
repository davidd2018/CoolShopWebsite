package com.example.CoolShopWebsite.dto.request;

import com.example.CoolShopWebsite.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateOrderStatusRequest {
    @NotNull private OrderStatus status;
}
