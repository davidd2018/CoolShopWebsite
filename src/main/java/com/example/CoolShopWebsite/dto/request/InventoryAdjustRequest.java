package com.example.CoolShopWebsite.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InventoryAdjustRequest {
    @NotNull
    private Integer adjustment; // dương: nhập kho, âm: xuất kho
}
