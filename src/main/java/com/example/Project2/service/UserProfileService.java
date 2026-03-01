package com.example.Project2.service;

import com.example.Project2.dto.request.ChangePasswordRequest;
import com.example.Project2.dto.request.UserProfileRequest;
import com.example.Project2.dto.response.UserProfileResponse;
import com.example.Project2.entity.UserProfile;
import com.example.Project2.entity.Users;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.UserProfileRepository;
import com.example.Project2.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void CreateEmptyProfile(Users users){
        UserProfile userProfile = UserProfile.builder()
                .user(users)
                .build();
        userProfileRepository.save(userProfile);
    }

    public UserProfileResponse getProfile(String username) {

        Users user = userRepository.findByUsername(username).
                orElseThrow(() ->new RuntimeException("Not Found"));

        UserProfile userProfile = userProfileRepository.findByUser(user).
                orElseThrow(() -> new RuntimeException("Not Found"));

        return UserProfileResponse.builder()
                .fullName(userProfile.getFullName())
                .phoneNumber(userProfile.getPhoneNumber())
                .email(userProfile.getEmail())
                .address(userProfile.getAddress())
                .build();

    }


    public UserProfileResponse updateProfile(String username, UserProfileRequest request) {

        Users user = userRepository.findByUsername(username).
                orElseThrow(() ->new RuntimeException("Not Found"));

        UserProfile userProfile = userProfileRepository.findByUser(user).
                orElseThrow(() -> new RuntimeException("Not Found"));

        userProfile.setFullName(request.getFullName());
        userProfile.setPhoneNumber(request.getPhoneNumber());
        userProfile.setEmail(request.getEmail());
        userProfile.setAddress(request.getAddress());

        userProfileRepository.save(userProfile);

        return UserProfileResponse.builder()
                .fullName(userProfile.getFullName())
                .phoneNumber(userProfile.getPhoneNumber())
                .email(userProfile.getEmail())
                .address(userProfile.getAddress())
                .build();
    }

    public void ChangePassword(String username , ChangePasswordRequest request){
        Users users = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Not Found"));

        if(!passwordEncoder.matches(request.getOldPassword() , users.getPassword())){
            throw new AppBadRequestException("Mat khau cu khong dung");
        }

        if(passwordEncoder.matches(request.getNewPassword() , users.getPassword())){
            throw new AppBadRequestException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        users.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(users);

    }
}
