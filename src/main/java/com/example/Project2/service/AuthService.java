package com.example.Project2.service;

import com.example.Project2.Enum.Role;
import com.example.Project2.dto.request.LoginRequest;
import com.example.Project2.dto.request.RefreshTokenRequest;
import com.example.Project2.dto.request.UserRequest;
import com.example.Project2.dto.response.LoginResponse;
import com.example.Project2.dto.response.RefreshTokenResponse;
import com.example.Project2.entity.RefreshToken;
import com.example.Project2.entity.Users;
import com.example.Project2.repository.UserRepository;
import com.example.Project2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserProfileService userProfileService;

    public String register(UserRequest userRequest) {

        if(userRepository.existsByUsername(userRequest.getUsername())){
            return "Fail";
        }

        Users users = new Users();
        users.setUsername(userRequest.getUsername());
        String password = passwordEncoder.encode(userRequest.getPassword());
        users.setPassword(password);
        users.setRole(Role.USER);
        userRepository.save(users);

        userProfileService.CreateEmptyProfile(users);

        return "Success";
    }

    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        Users users = userRepository.findByUsername(loginRequest.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        String accessToken = jwtUtil.generateAccessToken(users.getUsername(), users.getRole().name());
        RefreshToken refeshToken = refreshTokenService.createRefreshToken(users);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refeshToken.getToken())
                .username(users.getUsername())
                .role(users.getRole())
                .build();
    }

    // Refresh accessToken
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        Users user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken()) // giữ nguyên refreshToken cũ
                .build();
    }


    // Logout
    public void logout(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        refreshTokenService.deleteByUser(user);
    }

}
