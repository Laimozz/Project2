package com.example.Project2.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    @NotNull(message = "productId không được để trống")
    private Integer productId;

    private Integer quantity;
}
