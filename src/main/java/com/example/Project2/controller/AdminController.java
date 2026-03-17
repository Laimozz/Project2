package com.example.Project2.controller;

import com.example.Project2.dto.request.UpdateOrderStatusRequest;
import com.example.Project2.dto.response.AdminOrderResponse;
import com.example.Project2.dto.response.AdminUserResponse;
import com.example.Project2.service.AdminOrderService;
import com.example.Project2.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "https://feforproject2.vercel.app")
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<AdminOrderResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page , size);
        return ResponseEntity.ok(adminOrderService.getAllOrders(status, pageable));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<AdminOrderResponse> getOrderDetail(@PathVariable int id){
        return ResponseEntity.ok(adminOrderService.getOrderDetail(id));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<AdminOrderResponse> updateOrderStatus(@PathVariable int id , @RequestBody UpdateOrderStatusRequest request){
        return ResponseEntity.ok((adminOrderService.updateOrderStatus(id , request.getStatus())));
    }
}

