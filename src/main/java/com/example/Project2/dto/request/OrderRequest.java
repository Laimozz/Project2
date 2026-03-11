package com.example.Project2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    /** Ghi chú (tùy chọn) */
    private String note;

    /** Phương thức thanh toán: "COD" (mặc định) hoặc "VNPAY" */
    private String paymentMethod;
}
