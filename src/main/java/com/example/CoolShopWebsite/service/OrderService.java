package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.dto.request.CreateOrderRequest;
import com.example.CoolShopWebsite.dto.response.OrderResponse;
import com.example.CoolShopWebsite.exception.AppException;
import com.example.CoolShopWebsite.model.*;
import com.example.CoolShopWebsite.model.enums.*;
import com.example.CoolShopWebsite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------------
    // Tạo đơn hàng – toàn bộ trong 1 transaction
    // ----------------------------------------------------------------
    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found"));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Build Order trước để có reference cho OrderItems
        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO) // sẽ cập nhật sau
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .shippingAddress(req.getShippingAddress())
                .note(req.getNote())
                .status(OrderStatus.PENDING)
                .build();

        for (CreateOrderRequest.OrderItemRequest itemReq : req.getItems()) {
            // Pessimistic lock tránh oversell
            Product product = productRepository.findByIdForUpdate(itemReq.getProductId())
                    .orElseThrow(() -> AppException.notFound(
                            "Product not found: " + itemReq.getProductId()));

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw AppException.badRequest("Product is not available: " + product.getName());
            }
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw AppException.badRequest(
                        "Insufficient stock for: " + product.getName()
                        + " (available: " + product.getStockQuantity() + ")");
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal  = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            items.add(oi);

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        order.setOrderItems(items);
        Order saved = orderRepository.save(order);

        // Tạo Payment
        Payment payment = Payment.builder()
                .order(saved)
                .paymentMethod(req.getPaymentMethod())
                .amount(total)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        return OrderResponse.from(orderRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    // ----------------------------------------------------------------
    // Xem đơn hàng
    // ----------------------------------------------------------------
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(o -> orderRepository.findByIdWithDetails(o.getId())
                        .map(OrderResponse::from).orElseThrow());
    }

    public Page<OrderResponse> findByUser(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found"));
        return orderRepository.findByUserId(user.getId(), pageable)
                .map(o -> orderRepository.findByIdWithDetails(o.getId())
                        .map(OrderResponse::from).orElseThrow());
    }

    public OrderResponse findById(Long id, String email, boolean isAdmin) {
        Order order = getWithDetails(id);
        if (!isAdmin && !order.getUser().getEmail().equals(email)) {
            throw AppException.forbidden("Access denied to this order");
        }
        return OrderResponse.from(order);
    }

    // ----------------------------------------------------------------
    // Cập nhật trạng thái (Admin)
    // ----------------------------------------------------------------
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = getWithDetails(id);
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return OrderResponse.from(orderRepository.save(order));
    }

    // ----------------------------------------------------------------
    // Hủy đơn hàng
    // ----------------------------------------------------------------
    @Transactional
    public OrderResponse cancel(Long id, String email, boolean isAdmin) {
        Order order = getWithDetails(id);

        if (!isAdmin && !order.getUser().getEmail().equals(email)) {
            throw AppException.forbidden("Access denied to this order");
        }

        OrderStatus current = order.getStatus();
        if (current == OrderStatus.CANCELLED) {
            throw AppException.badRequest("Order is already cancelled");
        }
        if (!isAdmin && current != OrderStatus.PENDING) {
            throw AppException.badRequest("Cannot cancel order in status: " + current);
        }
        if (current == OrderStatus.COMPLETED || current == OrderStatus.SHIPPING) {
            throw AppException.badRequest("Cannot cancel order in status: " + current);
        }

        // Hoàn lại tồn kho (chỉ nếu đã trừ – tức là khi order đã tạo thành công)
        for (OrderItem oi : order.getOrderItems()) {
            Product product = oi.getProduct();
            product.setStockQuantity(product.getStockQuantity() + oi.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Cập nhật payment nếu có
        paymentRepository.findByOrderId(id).ifPresent(p -> {
            p.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(p);
        });

        return OrderResponse.from(orderRepository.save(order));
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private Order getWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> AppException.notFound("Order not found: " + id));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED  || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.SHIPPING   || next == OrderStatus.CANCELLED;
            case SHIPPING   -> next == OrderStatus.COMPLETED;
            default         -> false;
        };
        if (!valid) {
            throw AppException.badRequest(
                    "Invalid status transition: " + current + " → " + next);
        }
    }
}
