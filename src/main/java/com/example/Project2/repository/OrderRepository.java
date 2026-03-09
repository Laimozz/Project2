package com.example.Project2.repository;

import com.example.Project2.entity.Order;
import com.example.Project2.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order , Integer> {

    List<Order> findByUserOrderByOrderDateDesc(Users user);
}
