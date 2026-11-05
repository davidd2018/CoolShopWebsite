package com.example.CoolShopWebsite.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductRequest {
    @NotNull private Long categoryId;
    @NotBlank @Size(max = 255) private String name;
    private String description;
    @NotNull @DecimalMin("0.01") private BigDecimal price;
    private String imageUrl;
    @NotNull @Min(0) private Integer stockQuantity;
}
