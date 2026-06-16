package com.example.ecommerce.inventory;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryServiceInterface inventoryService;

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO created = inventoryService.createInventory(inventoryDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO> updateInventoryByProductId(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryDTO inventoryDTO
    ) {
        return ResponseEntity.ok(inventoryService.updateInventoryByProductId(productId, inventoryDTO));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStockInventories());
    }
}
