package com.example.CoolShopWebsite.controller;

import com.example.CoolShopWebsite.dto.request.CreateOrderRequest;
import com.example.CoolShopWebsite.dto.request.UpdateOrderStatusRequest;
import com.example.CoolShopWebsite.dto.response.ApiResponse;
import com.example.CoolShopWebsite.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** Customer đặt hàng */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CreateOrderRequest req,
                                                  @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created",
                        orderService.createOrder(principal.getUsername(), req)));
    }

    /** Admin xem tất cả, Customer xem của mình */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails principal) {

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        PageRequest pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        if (isAdmin) {
            return ResponseEntity.ok(ApiResponse.ok(orderService.findAll(pageable)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.findByUser(principal.getUsername(), pageable)));
    }

    /** Xem chi tiết đơn */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> get(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.findById(id, principal.getUsername(), isAdmin)));
    }

    /** Admin cập nhật trạng thái */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateOrderStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                orderService.updateStatus(id, req.getStatus())));
    }

    /** Hủy đơn */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> cancel(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled",
                orderService.cancel(id, principal.getUsername(), isAdmin)));
    }
}
