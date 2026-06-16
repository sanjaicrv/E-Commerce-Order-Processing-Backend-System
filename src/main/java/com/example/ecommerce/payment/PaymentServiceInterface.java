package com.example.ecommerce.payment;

public interface PaymentServiceInterface {
    PaymentDTO createPayment(PaymentDTO paymentDTO);
    PaymentDTO getPaymentById(Long id);
    PaymentDTO getPaymentByOrderId(Long orderId);
    PaymentDTO updatePaymentStatus(Long paymentId, PaymentStatus status);
}
