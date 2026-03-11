package com.example.Project2.service;

import com.example.Project2.Enum.OrderStatus;
import com.example.Project2.dto.response.AdminOrderResponse;
import com.example.Project2.dto.response.OrderItemResponse;
import com.example.Project2.entity.*;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;


    private String buildProductImageUrl(Product product) {
        if (product.getImageType() == null || product.getImageType().isBlank()) {
            return null;
        }
        return "/api/products/" + product.getId() + "/image";
    }


    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(buildProductImageUrl(item.getProduct()))
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

    private AdminOrderResponse toAdminOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        return AdminOrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .shippingAddress(order.getShippingAddress())
                .phone(order.getPhone())
                .note(order.getNote())
                .paymentMethod(order.getPaymentMethod())
                .items(itemResponses)
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .build();
    }

    public Page<AdminOrderResponse> getAllOrders(String status, Pageable pageable) {
        Page<Order> orders;

        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatusOrderByOrderDateDesc(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new AppBadRequestException("Trạng thái không hợp lệ: " + status);
            }
        } else {
            orders = orderRepository.findAllByOrderByOrderDateDesc(pageable);
        }

        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            return toAdminOrderResponse(order, items);
        });
    }

    /**
     * GET /api/admin/orders/{id}
     * Xem chi tiết 1 đơn hàng (Admin không cần kiểm tra quyền sở hữu)
     */
    public AdminOrderResponse getOrderDetail(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppBadRequestException(
                        "Không tìm thấy đơn hàng với ID: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return toAdminOrderResponse(order, items);
    }

    /**
     * PUT /api/admin/orders/{id}/status
     * Cập nhật trạng thái đơn hàng
     *
     * Luồng hợp lệ:
     *   PENDING          → CONFIRMED | CANCELLED
     *   AWAITING_PAYMENT → CONFIRMED | CANCELLED  (admin có thể xác nhận thủ công)
     *   CONFIRMED        → SHIPPING  | CANCELLED
     *   SHIPPING         → DELIVERED
     *   DELIVERED         → (không thể chuyển)
     *   CANCELLED         → (không thể chuyển)
     *
     * Khi CANCELLED: hoàn lại stock cho sản phẩm
     */
    @Transactional
    public AdminOrderResponse updateOrderStatus(Integer orderId, String newStatusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppBadRequestException(
                        "Không tìm thấy đơn hàng với ID: " + orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppBadRequestException("Trạng thái không hợp lệ: " + newStatusStr);
        }

        OrderStatus currentStatus = order.getStatus();

        // Validate state transition
        validateStatusTransition(currentStatus, newStatus);

        // Nếu chuyển sang CANCELLED → hoàn stock
        if (newStatus == OrderStatus.CANCELLED) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return toAdminOrderResponse(order, items);
    }

    /**
     * Validate chuyển trạng thái hợp lệ:
     *   PENDING          → CONFIRMED | CANCELLED
     *   AWAITING_PAYMENT → CONFIRMED | CANCELLED
     *   CONFIRMED        → SHIPPING  | CANCELLED
     *   SHIPPING         → DELIVERED
     *   DELIVERED         → (không thể chuyển)
     *   CANCELLED         → (không thể chuyển)
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING          -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case AWAITING_PAYMENT -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED        -> next == OrderStatus.SHIPPING  || next == OrderStatus.CANCELLED;
            case SHIPPING         -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new AppBadRequestException(
                    "Không thể chuyển trạng thái từ " + current.name() + " sang " + next.name());
        }
    }
}
