package com.example.Project2.service;

import com.example.Project2.entity.RefreshToken;
import com.example.Project2.entity.Users;
import com.example.Project2.exception.TokenExpiredException;
import com.example.Project2.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // Tạo mới refreshToken, mỗi user chỉ có 1 token (xóa cũ tạo mới)
    @Transactional
    public RefreshToken createRefreshToken(Users user) {
        // Xóa token cũ nếu có
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }


    // Tìm token trong DB
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenExpiredException("Refresh token không tồn tại"));
    }

    // Kiểm tra còn hạn không
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }
        return token;
    }

    // Logout → xóa token
    @Transactional
    public void deleteByUser(Users user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
