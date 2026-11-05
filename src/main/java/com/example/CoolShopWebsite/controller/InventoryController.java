package com.example.CoolShopWebsite.controller;

import com.example.CoolShopWebsite.dto.request.InventoryAdjustRequest;
import com.example.CoolShopWebsite.dto.response.ApiResponse;
import com.example.CoolShopWebsite.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class InventoryController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                productService.inventoryList(PageRequest.of(page, limit))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.inventoryDetail(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> adjust(@PathVariable Long id,
                                                  @Valid @RequestBody InventoryAdjustRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted",
                productService.adjustStock(id, req)));
    }
}
