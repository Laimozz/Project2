package com.example.Project2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "RefreshToken not allow empty")
    private String refreshToken;
}
