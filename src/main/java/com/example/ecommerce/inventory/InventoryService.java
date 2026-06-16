package com.example.ecommerce.inventory;

import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.product.ProductEntity;
import com.example.ecommerce.product.ProductJpa;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService implements InventoryServiceInterface {

    @Autowired
    private InventoryJpa inventoryJpa;

    @Autowired
    private ProductJpa productJpa;

    // ConcurrentHashMap for thread-safe O(1) stock lookup cache
    private final Map<Long, Integer> stockCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadInventoryCache() {
        List<InventoryEntity> allInventory = inventoryJpa.findAll();
        for (InventoryEntity item : allInventory) {
            stockCache.put(item.getProduct().getProductId(), item.getAvailableQuantity());
        }
    }

    @Override
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        ProductEntity product = productJpa.findById(inventoryDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + inventoryDTO.getProductId()));

        if (inventoryJpa.findByProductProductId(inventoryDTO.getProductId()).isPresent()) {
            throw new IllegalArgumentException("Inventory already exists for product id: " + inventoryDTO.getProductId());
        }

        InventoryEntity inventory = InventoryEntity.builder()
                .product(product)
                .availableQuantity(inventoryDTO.getAvailableQuantity())
                .reservedQuantity(inventoryDTO.getReservedQuantity() != null ? inventoryDTO.getReservedQuantity() : 0)
                .reorderLevel(inventoryDTO.getReorderLevel() != null ? inventoryDTO.getReorderLevel() : 10)
                .build();

        InventoryEntity saved = inventoryJpa.save(inventory);
        
        // Sync cache
        stockCache.put(product.getProductId(), saved.getAvailableQuantity());
        
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getAllInventories() {
        return inventoryJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(Long productId) {
        InventoryEntity inventory = inventoryJpa.findByProductProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Inventory not found for product id: " + productId));
        return convertToDTO(inventory);
    }

    @Override
    public InventoryDTO updateInventoryByProductId(Long productId, InventoryDTO inventoryDTO) {
        InventoryEntity inventory = inventoryJpa.findByProductProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Inventory not found for product id: " + productId));

        inventory.setAvailableQuantity(inventoryDTO.getAvailableQuantity());
        inventory.setReservedQuantity(inventoryDTO.getReservedQuantity());
        inventory.setReorderLevel(inventoryDTO.getReorderLevel());

        InventoryEntity updated = inventoryJpa.save(inventory);

        // Sync cache
        stockCache.put(productId, updated.getAvailableQuantity());

        return convertToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowStockInventories() {
        return inventoryJpa.findLowStock().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // DSA O(1) cache methods
    @Override
    public Integer getAvailableQuantityFromCache(Long productId) {
        return stockCache.getOrDefault(productId, 0);
    }

    @Override
    public void updateCache(Long productId, java.lang.Integer quantity) {
        stockCache.put(productId, quantity);
    }

    private InventoryDTO convertToDTO(InventoryEntity entity) {
        return InventoryDTO.builder()
                .inventoryId(entity.getInventoryId())
                .productId(entity.getProduct().getProductId())
                .availableQuantity(entity.getAvailableQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .reorderLevel(entity.getReorderLevel())
                .build();
    }
}
