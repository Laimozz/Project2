package com.example.Project2.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse {
    private Integer id;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String phone;
    private String note;
    private String paymentMethod;
    private List<OrderItemResponse> items;

    // Thông tin khách hàng — chỉ có ở admin response
    private Integer userId;
    private String username;
}
