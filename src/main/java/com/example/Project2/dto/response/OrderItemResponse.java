package com.example.Project2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Integer id;
    private Integer productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price;
}
