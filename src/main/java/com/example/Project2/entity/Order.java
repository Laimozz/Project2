package com.example.Project2.entity;

import com.example.Project2.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalPrice;

    private String shippingAddress;

    private String phone;

    private String note;

    // ── VNPay fields ──
    private String paymentMethod;   // "COD" hoặc "VNPAY"

    @Column(unique = true)
    private String vnpTxnRef;       // Mã giao dịch VNPay (unique, nullable)
}
