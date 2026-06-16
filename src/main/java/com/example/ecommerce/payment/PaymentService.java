package com.example.ecommerce.payment;

import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.PaymentFailedException;
import com.example.ecommerce.order.OrderEntity;
import com.example.ecommerce.order.OrderJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PaymentService implements PaymentServiceInterface {

    @Autowired
    private PaymentJpa paymentJpa;

    @Autowired
    private OrderJpa orderJpa;

    @Override
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        OrderEntity order = orderJpa.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + paymentDTO.getOrderId()));

        if (paymentJpa.findByOrderOrderId(paymentDTO.getOrderId()).isPresent()) {
            throw new IllegalArgumentException("Payment already registered for order id: " + paymentDTO.getOrderId());
        }

        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .paymentMethod(paymentDTO.getPaymentMethod())
                .paymentStatus(paymentDTO.getPaymentStatus() != null ? paymentDTO.getPaymentStatus() : PaymentStatus.PENDING)
                .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        PaymentEntity saved = paymentJpa.save(payment);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        PaymentEntity payment = paymentJpa.findById(id)
                .orElseThrow(() -> new PaymentFailedException("Payment record not found with id: " + id));
        return convertToDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        PaymentEntity payment = paymentJpa.findByOrderOrderId(orderId)
                .orElseThrow(() -> new PaymentFailedException("Payment record not found for order id: " + orderId));
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO updatePaymentStatus(Long paymentId, PaymentStatus status) {
        PaymentEntity payment = paymentJpa.findById(paymentId)
                .orElseThrow(() -> new PaymentFailedException("Payment record not found with id: " + paymentId));

        payment.setPaymentStatus(status);
        PaymentEntity updated = paymentJpa.save(payment);
        return convertToDTO(updated);
    }

    private PaymentDTO convertToDTO(PaymentEntity entity) {
        return PaymentDTO.builder()
                .paymentId(entity.getPaymentId())
                .orderId(entity.getOrder().getOrderId())
                .paymentMethod(entity.getPaymentMethod())
                .paymentStatus(entity.getPaymentStatus())
                .transactionReference(entity.getTransactionReference())
                .build();
    }
}
