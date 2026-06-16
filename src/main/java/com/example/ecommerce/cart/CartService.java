package com.example.ecommerce.cart;

import com.example.ecommerce.cartitem.CartItemDTO;
import com.example.ecommerce.cartitem.CartItemEntity;
import com.example.ecommerce.exception.CartNotFoundException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.user.UserEntity;
import com.example.ecommerce.user.UserJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService implements CartServiceInterface {

    @Autowired
    private CartJpa cartJpa;

    @Autowired
    private UserJpa userJpa;

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        CartEntity cart = cartJpa.findByUserUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user id: " + userId));
        return convertToDTO(cart);
    }

    @Override
    public CartDTO createCart(Long userId) {
        UserEntity user = userJpa.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (cartJpa.findByUserUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Cart already exists for user id: " + userId);
        }

        CartEntity cart = CartEntity.builder()
                .user(user)
                .build();

        CartEntity saved = cartJpa.save(cart);
        return convertToDTO(saved);
    }

    @Override
    public void deleteCart(Long cartId) {
        CartEntity cart = cartJpa.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));
        
        // Clear items, maintaining the cart container itself for the user
        cart.getItems().clear();
        cartJpa.save(cart);
    }

    private CartDTO convertToDTO(CartEntity entity) {
        List<CartItemDTO> itemDTOs = entity.getItems().stream()
                .map(item -> CartItemDTO.builder()
                        .cartItemId(item.getCartItemId())
                        .cartId(item.getCart().getCartId())
                        .productId(item.getProduct().getProductId())
                        .quantity(item.getQuantity())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());

        return CartDTO.builder()
                .cartId(entity.getCartId())
                .userId(entity.getUser().getUserId())
                .items(itemDTOs)
                .build();
    }
}
