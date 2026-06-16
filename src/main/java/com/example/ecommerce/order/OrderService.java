package com.example.ecommerce.order;

import com.example.ecommerce.address.AddressEntity;
import com.example.ecommerce.address.AddressJpa;
import com.example.ecommerce.cart.CartEntity;
import com.example.ecommerce.cart.CartJpa;
import com.example.ecommerce.cartitem.CartItemEntity;
import com.example.ecommerce.exception.AddressNotFoundException;
import com.example.ecommerce.exception.CartNotFoundException;
import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.OutOfStockException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.inventory.InventoryEntity;
import com.example.ecommerce.inventory.InventoryJpa;
import com.example.ecommerce.inventory.InventoryServiceInterface;
import com.example.ecommerce.orderitem.OrderItemDTO;
import com.example.ecommerce.orderitem.OrderItemEntity;
import com.example.ecommerce.orderitem.OrderItemJpa;
import com.example.ecommerce.payment.PaymentEntity;
import com.example.ecommerce.payment.PaymentJpa;
import com.example.ecommerce.payment.PaymentStatus;
import com.example.ecommerce.product.ProductEntity;
import com.example.ecommerce.user.UserEntity;
import com.example.ecommerce.user.UserJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements OrderServiceInterface {

    @Autowired
    private OrderJpa orderJpa;

    @Autowired
    private OrderItemJpa orderItemJpa;

    @Autowired
    private UserJpa userJpa;

    @Autowired
    private AddressJpa addressJpa;

    @Autowired
    private CartJpa cartJpa;

    @Autowired
    private PaymentJpa paymentJpa;

    @Autowired
    private InventoryJpa inventoryJpa;

    @Autowired
    private InventoryServiceInterface inventoryService;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        // 1. Validate User, Address, and Cart
        UserEntity user = userJpa.findById(orderDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + orderDTO.getUserId()));

        AddressEntity address = addressJpa.findById(orderDTO.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + orderDTO.getAddressId()));

        CartEntity cart = cartJpa.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user id: " + user.getUserId()));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with an empty cart");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .address(address)
                .orderStatus(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // 2. Validate Inventory and Reserve Stock
        for (CartItemEntity cartItem : cart.getItems()) {
            ProductEntity product = cartItem.getProduct();
            InventoryEntity inventory = product.getInventory();
            if (inventory == null) {
                throw new OutOfStockException("No inventory tracking config found for product: " + product.getName());
            }

            int requestedQty = cartItem.getQuantity();
            int dbAvailable = inventory.getAvailableQuantity();

            if (requestedQty > dbAvailable) {
                throw new OutOfStockException("Insufficient stock for product " + product.getName() + 
                        ". Available: " + dbAvailable + ", Requested: " + requestedQty);
            }

            // Reserve Stock: Decrement available, Increment reserved
            inventory.setAvailableQuantity(dbAvailable - requestedQty);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + requestedQty);
            inventoryJpa.save(inventory);

            // Sync inventory cache
            inventoryService.updateCache(product.getProductId(), inventory.getAvailableQuantity());

            // Compile item details
            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(requestedQty));
            total = total.add(itemTotal);

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestedQty)
                    .unitPrice(itemPrice)
                    .build();
            orderItems.add(orderItem);
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);

        // 3. Create Order & 4. Save Order Items
        OrderEntity savedOrder = orderJpa.save(order);

        // 5. Create Payment record
        String paymentMethod = orderDTO.getPaymentMethod() != null ? orderDTO.getPaymentMethod() : "CREDIT_CARD";
        PaymentEntity payment = PaymentEntity.builder()
                .order(savedOrder)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        paymentJpa.save(payment);
        savedOrder.setPayment(payment);

        // 6. Update Inventory: Commit stock (Deduct from reserved quantity)
        for (OrderItemEntity orderItem : orderItems) {
            ProductEntity product = orderItem.getProduct();
            InventoryEntity inventory = product.getInventory();
            inventory.setReservedQuantity(inventory.getReservedQuantity() - orderItem.getQuantity());
            inventoryJpa.save(inventory);
        }

        // 7. Clear Cart items
        cart.getItems().clear();
        cartJpa.save(cart);

        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        OrderEntity order = orderJpa.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        if (!userJpa.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        return orderJpa.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = orderJpa.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        order.setOrderStatus(status);
        
        // If order gets cancelled, refund stock
        if (status == OrderStatus.CANCELLED) {
            for (OrderItemEntity item : order.getItems()) {
                ProductEntity product = item.getProduct();
                InventoryEntity inventory = product.getInventory();
                if (inventory != null) {
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + item.getQuantity());
                    inventoryJpa.save(inventory);
                    inventoryService.updateCache(product.getProductId(), inventory.getAvailableQuantity());
                }
            }
        }

        OrderEntity updated = orderJpa.save(order);
        return convertToDTO(updated);
    }

    @Override
    public void deleteOrder(Long orderId) {
        OrderEntity order = orderJpa.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderJpa.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrdersPaginated(Pageable pageable) {
        Page<OrderEntity> page = orderJpa.findAll(pageable);
        List<OrderDTO> list = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    // Top Selling Leaderboard using custom Priority Queue Max Heap DSA
    @Override
    @Transactional(readOnly = true)
    public List<ProductSalesDTO> getTopSellingProducts(int k) {
        List<OrderItemEntity> orderItems = orderItemJpa.findAll();
        Map<ProductEntity, Integer> salesAggregate = new HashMap<>();

        for (OrderItemEntity item : orderItems) {
            ProductEntity prod = item.getProduct();
            salesAggregate.put(prod, salesAggregate.getOrDefault(prod, 0) + item.getQuantity());
        }

        // Build Max Heap PriorityQueue
        PriorityQueue<ProductSalesDTO> maxHeap = new PriorityQueue<>(
                (a, b) -> Integer.compare(b.getTotalQuantitySold(), a.getTotalQuantitySold())
        );

        for (Map.Entry<ProductEntity, java.lang.Integer> entry : salesAggregate.entrySet()) {
            ProductEntity prod = entry.getKey();
            maxHeap.offer(ProductSalesDTO.builder()
                    .productId(prod.getProductId())
                    .productName(prod.getName())
                    .totalQuantitySold(entry.getValue())
                    .build());
        }

        List<ProductSalesDTO> resultList = new ArrayList<>();
        int count = 0;
        while (!maxHeap.isEmpty() && count < k) {
            resultList.add(maxHeap.poll());
            count++;
        }

        return resultList;
    }

    private OrderDTO convertToDTO(OrderEntity entity) {
        List<OrderItemDTO> itemsDTO = entity.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .orderItemId(item.getOrderItemId())
                        .orderId(item.getOrder().getOrderId())
                        .productId(item.getProduct().getProductId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(entity.getOrderId())
                .userId(entity.getUser().getUserId())
                .addressId(entity.getAddress().getAddressId())
                .orderStatus(entity.getOrderStatus())
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .items(itemsDTO)
                .build();
    }
}
