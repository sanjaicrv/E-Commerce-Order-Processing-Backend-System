package com.example.ecommerce.cartitem;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartOperation {
    public enum Type {
        ADD,
        UPDATE,
        DELETE
    }

    private final Type type;
    private final Long cartId;
    private final Long productId;
    private final Integer quantity; // For UPDATE, represents the previous quantity. For ADD, represents the quantity added. For DELETE, represents the deleted quantity.
}
