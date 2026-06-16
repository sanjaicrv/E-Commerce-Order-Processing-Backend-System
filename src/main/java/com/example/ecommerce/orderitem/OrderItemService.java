package com.example.ecommerce.orderitem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderItemService implements OrderItemServiceInterface {

    @Autowired
    private OrderItemJpa orderItemJpa;

    @Override
    public OrderItemDTO getOrderItemById(Long id) {
        OrderItemEntity item = orderItemJpa.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found with id: " + id));
        return convertToDTO(item);
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        return orderItemJpa.findByOrderOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderItemDTO convertToDTO(OrderItemEntity entity) {
        return OrderItemDTO.builder()
                .orderItemId(entity.getOrderItemId())
                .orderId(entity.getOrder().getOrderId())
                .productId(entity.getProduct().getProductId())
                .productName(entity.getProduct().getName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .build();
    }
}
