package com.example.Project2.dto.response;


import com.example.Project2.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String username;
    private Role role;
    private String accessToken;
    private String refreshToken;
}
