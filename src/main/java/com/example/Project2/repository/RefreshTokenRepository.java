package com.example.Project2.repository;

import com.example.Project2.entity.RefreshToken;
import com.example.Project2.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken , Integer> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(Users user);


}
