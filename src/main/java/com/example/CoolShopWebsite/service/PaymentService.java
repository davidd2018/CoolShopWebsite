package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.dto.response.PaymentResponse;
import com.example.CoolShopWebsite.exception.AppException;
import com.example.CoolShopWebsite.model.Payment;
import com.example.CoolShopWebsite.model.enums.PaymentStatus;
import com.example.CoolShopWebsite.repository.OrderRepository;
import com.example.CoolShopWebsite.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Page<PaymentResponse> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(PaymentResponse::from);
    }

    public PaymentResponse findById(Long id) {
        return PaymentResponse.from(getOrThrow(id));
    }

    public PaymentResponse findByOrder(Long orderId, String email, boolean isAdmin) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found"));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> AppException.notFound("Payment not found for order: " + orderId));
        if (!isAdmin && !payment.getOrder().getUser().getEmail().equals(email)) {
            throw AppException.forbidden("Access denied");
        }
        return PaymentResponse.from(payment);
    }

    public PaymentResponse updateStatus(Long id, PaymentStatus newStatus) {
        Payment payment = getOrThrow(id);
        validateTransition(payment.getPaymentStatus(), newStatus);
        payment.setPaymentStatus(newStatus);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    private void validateTransition(PaymentStatus current, PaymentStatus next) {
        boolean valid = switch (current) {
            case PENDING  -> next == PaymentStatus.PAID || next == PaymentStatus.FAILED;
            case PAID     -> next == PaymentStatus.REFUNDED;
            default       -> false;
        };
        if (!valid) {
            throw AppException.badRequest("Invalid payment status transition: " + current + " → " + next);
        }
    }

    private Payment getOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Payment not found: " + id));
    }
}
