package com.example.ecommerce.orderitem;

import java.util.List;

public interface OrderItemServiceInterface {
    OrderItemDTO getOrderItemById(Long id);
    List<OrderItemDTO> getOrderItemsByOrderId(Long orderId);
}
