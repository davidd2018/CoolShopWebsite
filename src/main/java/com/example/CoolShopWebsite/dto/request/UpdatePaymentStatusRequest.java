package com.example.CoolShopWebsite.dto.request;

import com.example.CoolShopWebsite.model.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdatePaymentStatusRequest {
    @NotNull private PaymentStatus status;
}
