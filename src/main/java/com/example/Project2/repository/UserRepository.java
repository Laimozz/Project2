package com.example.Project2.repository;

import com.example.Project2.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByUsername(String username);
    Boolean existsByUsername(String username);

    Page<Users> findAll(Pageable pageable);
}
