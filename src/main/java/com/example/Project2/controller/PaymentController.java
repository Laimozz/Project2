package com.example.Project2.controller;

import com.example.Project2.Enum.OrderStatus;
import com.example.Project2.config.VnPayConfig;
import com.example.Project2.dto.response.PaymentResponse;
import com.example.Project2.entity.Order;
import com.example.Project2.entity.OrderItem;
import com.example.Project2.entity.Product;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.OrderItemRepository;
import com.example.Project2.repository.ProductRepository;
import com.example.Project2.service.OrderService;
import com.example.Project2.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "https://feforproject2.vercel.app")
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VnPayConfig vnPayConfig;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * POST /api/payment/create?orderId={id}
     * Tạo URL thanh toán VNPay cho 1 đơn hàng
     * Chỉ tạo khi order có paymentMethod = VNPAY và status = AWAITING_PAYMENT
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestParam Integer orderId,
            HttpServletRequest request) {

        // Kiểm tra user sở hữu đơn
        String username = getCurrentUsername();
        Order order = orderService.findOrderById(orderId);

        if (!order.getUser().getUsername().equals(username)) {
            throw new AppBadRequestException("Đơn hàng này không thuộc về bạn");
        }
        if (!"VNPAY".equals(order.getPaymentMethod())) {
            throw new AppBadRequestException("Đơn hàng này không dùng phương thức VNPay");
        }
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new AppBadRequestException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // Lấy IP client
        String ipAddress = getClientIp(request);

        // Tạo URL
        String paymentUrl = vnPayService.createPaymentUrl(order, ipAddress);

        return ResponseEntity.ok(PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .build());
    }

    /**
     * GET /api/payment/callback?vnp_xxx=...
     * VNPay redirect về URL này sau khi thanh toán
     * Endpoint này PHẢI public (không cần JWT)
     *
     * Sau khi xử lý, redirect FE tới trang kết quả
     */
    @GetMapping("/callback")
    @Transactional
    public ResponseEntity<Void> vnpayCallback(@RequestParam Map<String, String> params) {
        String vnpTxnRef = params.get("vnp_TxnRef");
        Order order = orderService.findByVnpTxnRef(vnpTxnRef);

        // Chỉ xử lý nếu đơn đang ở AWAITING_PAYMENT (tránh xử lý 2 lần)
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            boolean validChecksum = vnPayService.validateCallback(params);
            boolean paymentSuccess = validChecksum && vnPayService.isPaymentSuccess(params);

            if (paymentSuccess) {
                order.setStatus(OrderStatus.CONFIRMED);
            } else {
                // Thanh toán thất bại → hủy đơn, hoàn stock
                order.setStatus(OrderStatus.CANCELLED);
                restoreStock(order);
            }
            // Lưu lại (order đã trong transaction context)
        }

        // Redirect FE → trang kết quả
        String responseCode = params.getOrDefault("vnp_ResponseCode", "99");
        String feRedirectUrl = UriComponentsBuilder
                .fromUriString(vnPayConfig.getFrontendUrl())
                .path("/payment/result")
                .queryParam("vnp_ResponseCode", responseCode)
                .queryParam("vnp_TxnRef", vnpTxnRef)
                .queryParam("orderId", order.getId())
                .build()
                .toUriString();

        return ResponseEntity.status(302)
                .header("Location", feRedirectUrl)
                .build();
    }

    // ── Helper ──

    private void restoreStock(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
            ip = ip.split(",")[0].trim();
        }
        // Localhost IPv6 → IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}
