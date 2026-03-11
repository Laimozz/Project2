package com.example.Project2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Integer id;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String phone;
    private String note;
    private String paymentMethod;
    private List<OrderItemResponse> items;
}
