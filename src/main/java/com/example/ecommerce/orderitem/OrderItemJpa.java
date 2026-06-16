package com.example.ecommerce.orderitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemJpa extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderOrderId(Long orderId);
}
