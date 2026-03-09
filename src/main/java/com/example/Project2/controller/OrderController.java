package com.example.Project2.controller;

import com.example.Project2.dto.request.OrderRequest;
import com.example.Project2.dto.response.OrderResponse;
import com.example.Project2.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * POST /api/orders/checkout — Đặt hàng từ giỏ hàng (COD)
     */
    @PostMapping("/orders/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody OrderRequest request) {
        String username = getCurrentUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(username, request));
    }

    /**
     * GET /api/orders — Lấy danh sách đơn hàng của user
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(orderService.getMyOrders(username));
    }

    /**
     * GET /api/orders/{id} — Xem chi tiết 1 đơn hàng
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Integer id) {
        String username = getCurrentUsername();
        return ResponseEntity.ok(orderService.getOrderDetail(username, id));
    }

    /**
     * PUT /api/orders/{id}/cancel — Hủy đơn hàng (chỉ khi PENDING)
     */
    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Integer id) {
        String username = getCurrentUsername();
        return ResponseEntity.ok(orderService.cancelOrder(username, id));
    }
}
