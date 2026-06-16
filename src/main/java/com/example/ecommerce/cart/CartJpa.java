package com.example.ecommerce.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartJpa extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUserUserId(Long userId);
    Optional<CartEntity> findByUserEmail(String email);
}
