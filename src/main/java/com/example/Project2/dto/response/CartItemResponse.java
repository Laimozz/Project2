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
public class CartItemResponse {

    /** ID của CartItem (dùng cho PUT/DELETE) */
    private Integer id;
    /** ID sản phẩm */
    private Integer productId;
    /** Tên sản phẩm */
    private String productName;
    /** Giá sản phẩm */
    private BigDecimal productPrice;
    /** Ảnh sản phẩm */
    private String productImageUrl;
    /** Số lượng trong giỏ */
    private Integer quantity;

}
