package com.example.ecommerce.order;

import com.example.ecommerce.orderitem.OrderItemDTO;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long orderId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Address ID is required")
    private Long addressId;

    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private List<OrderItemDTO> items;

    // Helper request parameters for checkout
    private String paymentMethod;
}
