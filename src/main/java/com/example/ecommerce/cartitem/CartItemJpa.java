package com.example.ecommerce.cartitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemJpa extends JpaRepository<CartItemEntity, Long> {
    Optional<CartItemEntity> findByCartCartIdAndProductProductId(Long cartId, Long productId);
    List<CartItemEntity> findByCartCartId(Long cartId);
}
