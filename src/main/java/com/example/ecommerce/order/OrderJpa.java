package com.example.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpa extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserUserId(Long userId);
}
