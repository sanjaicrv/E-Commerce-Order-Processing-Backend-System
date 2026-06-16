package com.example.ecommerce.cart;

public interface CartServiceInterface {
    CartDTO getCartByUserId(Long userId);
    CartDTO createCart(Long userId);
    void deleteCart(Long cartId);
}
