package com.example.Project2.repository;

import com.example.Project2.Enum.OrderStatus;
import com.example.Project2.entity.Order;
import com.example.Project2.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order , Integer> {

    List<Order> findByUserOrderByOrderDateDesc(Users user);


    // Admin: lấy tất cả đơn hàng, sắp xếp theo ngày mới nhất
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    // Admin: lọc theo trạng thái
    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);
}
