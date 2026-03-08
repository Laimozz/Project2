package com.example.Project2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    /** ID của giỏ hàng */
    private Integer id;
    /** Danh sách sản phẩm trong giỏ */
    private List<CartItemResponse> items;
    /** Tổng tiền toàn bộ giỏ hàng (sum of price * quantity) */
    private BigDecimal totalPrice;
}
