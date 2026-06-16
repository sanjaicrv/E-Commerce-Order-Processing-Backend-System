package com.example.ecommerce.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderServiceInterface {
    OrderDTO placeOrder(OrderDTO orderDTO);
    List<OrderDTO> getAllOrders();
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getOrdersByUserId(Long userId);
    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);
    void deleteOrder(Long orderId);
    Page<OrderDTO> getAllOrdersPaginated(Pageable pageable);
    
    // Max Heap PriorityQueue custom optimization
    List<ProductSalesDTO> getTopSellingProducts(int k);
}
