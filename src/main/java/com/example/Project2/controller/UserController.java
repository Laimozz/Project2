package com.example.Project2.controller;

import com.example.Project2.dto.request.ChangePasswordRequest;
import com.example.Project2.dto.request.UserProfileRequest;
import com.example.Project2.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/profile")
    public ResponseEntity<?> GetProfile(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(userProfileService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> UpdateProfile(@AuthenticationPrincipal UserDetails userDetails ,
        @RequestBody UserProfileRequest request){
        return ResponseEntity.ok(userProfileService.updateProfile(userDetails.getUsername() , request));
        }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.ChangePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

}
