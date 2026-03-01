package com.example.Project2.repository;

import com.example.Project2.entity.UserProfile;
import com.example.Project2.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile , Integer> {
    Optional<UserProfile> findByUser(Users user);

    void deleteByUser(Users user);
}
