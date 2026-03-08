package com.example.Project2.repository;

import com.example.Project2.entity.Cart;
import com.example.Project2.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart , Integer> {
    Optional<Cart> findByUser(Users user);
}
