package com.example.ecommerce.cartitem;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartItemController {

    @Autowired
    private CartItemServiceInterface cartItemService;

    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addCartItem(@Valid @RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO added = cartItemService.addCartItem(cartItemDTO);
        return new ResponseEntity<>(added, HttpStatus.CREATED);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartItemDTO> updateCartItem(@PathVariable Long id, @Valid @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartItemService.updateCartItem(id, cartItemDTO));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long id) {
        cartItemService.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getCartItemsByCartId(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartItemService.getCartItems(cartId));
    }

    @PostMapping("/undo")
    public ResponseEntity<Void> undoLastCartOperation(@RequestParam Long userId) {
        cartItemService.undoLastCartOperation(userId);
        return ResponseEntity.ok().build();
    }
}
