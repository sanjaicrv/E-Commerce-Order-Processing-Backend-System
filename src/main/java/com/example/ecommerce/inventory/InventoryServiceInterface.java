package com.example.ecommerce.inventory;

import java.util.List;

public interface InventoryServiceInterface {
    InventoryDTO createInventory(InventoryDTO inventoryDTO);
    List<InventoryDTO> getAllInventories();
    InventoryDTO getInventoryByProductId(Long productId);
    InventoryDTO updateInventoryByProductId(Long productId, InventoryDTO inventoryDTO);
    List<InventoryDTO> getLowStockInventories();
    
    // Fast O(1) stock lookup methods using the memory cache
    Integer getAvailableQuantityFromCache(Long productId);
    void updateCache(Long productId, Integer quantity);
}
