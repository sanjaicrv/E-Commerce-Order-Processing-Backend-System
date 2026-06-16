package com.example.ecommerce.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryJpa extends JpaRepository<InventoryEntity, Long> {
    Optional<InventoryEntity> findByProductProductId(Long productId);

    @Query("SELECT i FROM InventoryEntity i WHERE i.availableQuantity <= i.reorderLevel")
    List<InventoryEntity> findLowStock();
}
