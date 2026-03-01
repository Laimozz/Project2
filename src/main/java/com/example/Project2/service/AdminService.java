package com.example.Project2.service;

import com.example.Project2.dto.response.AdminUserResponse;
import com.example.Project2.entity.UserProfile;
import com.example.Project2.entity.Users;
import com.example.Project2.repository.UserProfileRepository;
import com.example.Project2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> {
                    UserProfile profile = userProfileRepository
                            .findByUser(user).orElse(null);

                    return AdminUserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .role(user.getRole())
                            .fullName(profile != null ? profile.getFullName() : null)
                            .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                            .email(profile != null ? profile.getEmail() : null)
                            .build();
                });
    }

    public AdminUserResponse getUserById(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository
                .findByUser(user).orElse(null);

        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .fullName(profile != null ? profile.getFullName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .email(profile != null ? profile.getEmail() : null)
                .build();
    }

    public Map<String, String> deleteUser(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userProfileRepository.deleteByUser(user);
        userRepository.delete(user);

        return Map.of("message", "Xóa tài khoản thành công");
    }
}
