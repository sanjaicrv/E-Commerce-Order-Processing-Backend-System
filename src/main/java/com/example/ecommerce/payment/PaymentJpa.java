package com.example.ecommerce.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentJpa extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByOrderOrderId(Long orderId);
}
