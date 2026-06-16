package com.example.ecommerce.cartitem;

import com.example.ecommerce.cart.CartEntity;
import com.example.ecommerce.cart.CartJpa;
import com.example.ecommerce.exception.CartNotFoundException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.product.ProductEntity;
import com.example.ecommerce.product.ProductJpa;
import com.example.ecommerce.user.UserEntity;
import com.example.ecommerce.user.UserJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartItemService implements CartItemServiceInterface {

    @Autowired
    private CartItemJpa cartItemJpa;

    @Autowired
    private CartJpa cartJpa;

    @Autowired
    private ProductJpa productJpa;

    @Autowired
    private UserJpa userJpa;

    // Thread-safe map storing stacks of cart operations per userId for O(1) push/pop operations
    private final Map<Long, Stack<CartOperation>> undoStacks = new ConcurrentHashMap<>();

    private Stack<CartOperation> getUserStack(Long userId) {
        return undoStacks.computeIfAbsent(userId, k -> new Stack<>());
    }

    @Override
    public CartItemDTO addCartItem(CartItemDTO cartItemDTO) {
        CartEntity cart = cartJpa.findById(cartItemDTO.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartItemDTO.getCartId()));

        ProductEntity product = productJpa.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + cartItemDTO.getProductId()));

        Long userId = cart.getUser().getUserId();
        Stack<CartOperation> stack = getUserStack(userId);

        Optional<CartItemEntity> existingOpt = cartItemJpa.findByCartCartIdAndProductProductId(cart.getCartId(), product.getProductId());
        CartItemEntity item;
        
        if (existingOpt.isPresent()) {
            item = existingOpt.get();
            int prevQty = item.getQuantity();
            item.setQuantity(prevQty + cartItemDTO.getQuantity());
            
            // Push update operation with the previous quantity
            stack.push(new CartOperation(CartOperation.Type.UPDATE, cart.getCartId(), product.getProductId(), prevQty));
        } else {
            item = CartItemEntity.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(cartItemDTO.getQuantity())
                    .build();
            
            // Push add operation with quantity added
            stack.push(new CartOperation(CartOperation.Type.ADD, cart.getCartId(), product.getProductId(), cartItemDTO.getQuantity()));
        }

        CartItemEntity saved = cartItemJpa.save(item);
        return convertToDTO(saved);
    }

    @Override
    public CartItemDTO updateCartItem(Long cartItemId, CartItemDTO cartItemDTO) {
        CartItemEntity item = cartItemJpa.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + cartItemId));

        Long userId = item.getCart().getUser().getUserId();
        Stack<CartOperation> stack = getUserStack(userId);
        
        int prevQty = item.getQuantity();
        item.setQuantity(cartItemDTO.getQuantity());

        // Push update operation with previous quantity
        stack.push(new CartOperation(CartOperation.Type.UPDATE, item.getCart().getCartId(), item.getProduct().getProductId(), prevQty));

        CartItemEntity saved = cartItemJpa.save(item);
        return convertToDTO(saved);
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        CartItemEntity item = cartItemJpa.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + cartItemId));

        Long userId = item.getCart().getUser().getUserId();
        Stack<CartOperation> stack = getUserStack(userId);

        // Push delete operation with its deleted quantity
        stack.push(new CartOperation(CartOperation.Type.DELETE, item.getCart().getCartId(), item.getProduct().getProductId(), item.getQuantity()));

        cartItemJpa.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(Long cartId) {
        return cartItemJpa.findByCartCartId(cartId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void undoLastCartOperation(Long userId) {
        Stack<CartOperation> stack = getUserStack(userId);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("No operations left to undo for user: " + userId);
        }

        CartOperation op = stack.pop();
        CartEntity cart = cartJpa.findById(op.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + op.getCartId()));

        ProductEntity product = productJpa.findById(op.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + op.getProductId()));

        Optional<CartItemEntity> itemOpt = cartItemJpa.findByCartCartIdAndProductProductId(cart.getCartId(), product.getProductId());

        switch (op.getType()) {
            case ADD -> {
                // The action was adding. To undo, deduct the added quantity
                if (itemOpt.isPresent()) {
                    CartItemEntity item = itemOpt.get();
                    int finalQty = item.getQuantity() - op.getQuantity();
                    if (finalQty <= 0) {
                        cartItemJpa.delete(item);
                    } else {
                        item.setQuantity(finalQty);
                        cartItemJpa.save(item);
                    }
                }
            }
            case UPDATE -> {
                // The action was updating. To undo, restore the previous quantity
                if (itemOpt.isPresent()) {
                    CartItemEntity item = itemOpt.get();
                    item.setQuantity(op.getQuantity());
                    cartItemJpa.save(item);
                } else {
                    CartItemEntity item = CartItemEntity.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(op.getQuantity())
                            .build();
                    cartItemJpa.save(item);
                }
            }
            case DELETE -> {
                // The action was deleting. To undo, re-add the deleted item with its previous quantity
                if (itemOpt.isPresent()) {
                    CartItemEntity item = itemOpt.get();
                    item.setQuantity(item.getQuantity() + op.getQuantity());
                    cartItemJpa.save(item);
                } else {
                    CartItemEntity item = CartItemEntity.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(op.getQuantity())
                            .build();
                    cartItemJpa.save(item);
                }
            }
        }
    }

    private CartItemDTO convertToDTO(CartItemEntity entity) {
        return CartItemDTO.builder()
                .cartItemId(entity.getCartItemId())
                .cartId(entity.getCart().getCartId())
                .productId(entity.getProduct().getProductId())
                .quantity(entity.getQuantity())
                .productName(entity.getProduct().getName())
                .productPrice(entity.getProduct().getPrice())
                .build();
    }
}
