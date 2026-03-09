package com.example.Project2.repository;

import com.example.Project2.entity.Order;
import com.example.Project2.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem , Integer> {

    List<OrderItem> findByOrder(Order order);
}
