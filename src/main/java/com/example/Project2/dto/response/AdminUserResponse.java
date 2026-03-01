package com.example.Project2.dto.response;

import com.example.Project2.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {
    private Integer id;
    private String username;
    private Role role;
    private String fullName;
    private String phoneNumber;
    private String email;
}
