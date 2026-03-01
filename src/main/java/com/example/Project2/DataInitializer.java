package com.example.Project2;

import com.example.Project2.Enum.Role;
import com.example.Project2.entity.UserProfile;
import com.example.Project2.entity.Users;
import com.example.Project2.repository.UserProfileRepository;
import com.example.Project2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private final UserProfileRepository userProfileRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Kiểm tra nếu chưa có admin thì tạo
        if (!userRepository.existsByUsername("admin")) {
            Users admin = Users.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);

            userProfileRepository.save(UserProfile.builder()
                    .user(admin)
                    .build());

            System.out.println("✅ Admin account created!");
        }
    }
}