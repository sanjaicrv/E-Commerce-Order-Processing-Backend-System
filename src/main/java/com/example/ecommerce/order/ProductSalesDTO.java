package com.example.ecommerce.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSalesDTO {
    private Long productId;
    private String productName;
    private Integer totalQuantitySold;
}
