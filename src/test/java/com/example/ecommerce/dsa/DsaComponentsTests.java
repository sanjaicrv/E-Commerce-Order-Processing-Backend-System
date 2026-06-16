package com.example.ecommerce.dsa;

import com.example.ecommerce.cart.CartEntity;
import com.example.ecommerce.cart.CartJpa;
import com.example.ecommerce.cartitem.CartItemDTO;
import com.example.ecommerce.cartitem.CartItemServiceInterface;
import com.example.ecommerce.category.CategoryDTO;
import com.example.ecommerce.category.CategoryServiceInterface;
import com.example.ecommerce.inventory.InventoryDTO;
import com.example.ecommerce.inventory.InventoryServiceInterface;
import com.example.ecommerce.order.OrderDTO;
import com.example.ecommerce.order.OrderServiceInterface;
import com.example.ecommerce.order.ProductSalesDTO;
import com.example.ecommerce.product.ProductDTO;
import com.example.ecommerce.product.ProductServiceInterface;
import com.example.ecommerce.user.Role;
import com.example.ecommerce.user.UserDTO;
import com.example.ecommerce.user.UserServiceInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DsaComponentsTests {

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private CategoryServiceInterface categoryService;

    @Autowired
    private ProductServiceInterface productService;

    @Autowired
    private InventoryServiceInterface inventoryService;

    @Autowired
    private CartItemServiceInterface cartItemService;

    @Autowired
    private OrderServiceInterface orderService;

    @Autowired
    private CartJpa cartJpa;

    @Test
    public void testAllDsaOptimizations() {
        // 1. Setup mock Category
        CategoryDTO category = categoryService.createCategory(CategoryDTO.builder()
                .name("Electronics")
                .description("Gizmos")
                .build());

        // 2. Setup mock Products
        ProductDTO prod1 = productService.createProduct(ProductDTO.builder()
                .name("Smartphone A")
                .brand("Brand X")
                .price(new BigDecimal("99.99"))
                .categoryId(category.getCategoryId())
                .build());

        ProductDTO prod2 = productService.createProduct(ProductDTO.builder()
                .name("Smartphone B")
                .brand("Brand Y")
                .price(new BigDecimal("199.99"))
                .categoryId(category.getCategoryId())
                .build());

        // 3. Setup Inventories
        InventoryDTO inv1 = inventoryService.createInventory(InventoryDTO.builder()
                .productId(prod1.getProductId())
                .availableQuantity(50)
                .reservedQuantity(0)
                .reorderLevel(5)
                .build());

        InventoryDTO inv2 = inventoryService.createInventory(InventoryDTO.builder()
                .productId(prod2.getProductId())
                .availableQuantity(25)
                .reservedQuantity(0)
                .reorderLevel(2)
                .build());

        // Test HashMap stock lookup cache
        Assertions.assertEquals(50, inventoryService.getAvailableQuantityFromCache(prod1.getProductId()));
        Assertions.assertEquals(25, inventoryService.getAvailableQuantityFromCache(prod2.getProductId()));

        // Test Product sorting (Merge Sort)
        List<ProductDTO> sortedByPriceDesc = productService.getSortedProductsByPrice("desc");
        Assertions.assertEquals(prod2.getProductId(), sortedByPriceDesc.get(0).getProductId());
        Assertions.assertEquals(new BigDecimal("199.99"), sortedByPriceDesc.get(0).getPrice());

        // 4. Setup mock User
        UserDTO user = userService.registerUser(UserDTO.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build());

        CartEntity cart = cartJpa.findByUserUserId(user.getUserId()).orElseThrow();

        // 5. Test Stack-based Cart Item Undo
        CartItemDTO itemDTO = cartItemService.addCartItem(CartItemDTO.builder()
                .cartId(cart.getCartId())
                .productId(prod1.getProductId())
                .quantity(3)
                .build());

        List<CartItemDTO> cartItems = cartItemService.getCartItems(cart.getCartId());
        Assertions.assertEquals(1, cartItems.size());
        Assertions.assertEquals(3, cartItems.get(0).getQuantity());

        // Trigger undo of add operation
        cartItemService.undoLastCartOperation(user.getUserId());
        cartItems = cartItemService.getCartItems(cart.getCartId());
        Assertions.assertTrue(cartItems.isEmpty());
    }
}
