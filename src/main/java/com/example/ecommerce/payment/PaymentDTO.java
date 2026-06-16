package com.example.ecommerce.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionReference;
}
