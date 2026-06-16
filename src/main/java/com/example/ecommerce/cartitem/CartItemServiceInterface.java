package com.example.ecommerce.cartitem;

import java.util.List;

public interface CartItemServiceInterface {
    CartItemDTO addCartItem(CartItemDTO cartItemDTO);
    CartItemDTO updateCartItem(Long cartItemId, CartItemDTO cartItemDTO);
    void deleteCartItem(Long cartItemId);
    List<CartItemDTO> getCartItems(Long cartId);
    void undoLastCartOperation(Long userId);
}
