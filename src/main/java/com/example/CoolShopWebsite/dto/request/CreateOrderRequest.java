package com.example.CoolShopWebsite.dto.request;

import com.example.CoolShopWebsite.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateOrderRequest {
    @NotBlank @Size(max = 100) private String receiverName;
    @NotBlank @Size(max = 20)  private String receiverPhone;
    @NotBlank                  private String shippingAddress;
    private String note;

    @NotNull @Size(min = 1)
    @Valid
    private List<OrderItemRequest> items;

    @NotNull private PaymentMethod paymentMethod;

    @Getter
    public static class OrderItemRequest {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;
    }
}
