package com.example.Project2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // Giá trị: CONFIRMED, SHIPPING, DELIVERED, CANCELLED
}
