package com.example.Project2.Enum;

public enum OrderStatus {
    PENDING,           // Chờ xác nhận (COD)
    AWAITING_PAYMENT,  // Chờ thanh toán VNPay
    CONFIRMED,         // Đã xác nhận / Đã thanh toán
    SHIPPING,          // Đang giao hàng
    DELIVERED,         // Đã giao hàng
    CANCELLED          // Đã hủy
}
