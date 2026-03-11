package com.example.Project2.service;

import com.example.Project2.Enum.OrderStatus;
import com.example.Project2.config.VnPayConfig;
import com.example.Project2.dto.request.OrderRequest;
import com.example.Project2.dto.response.OrderItemResponse;
import com.example.Project2.dto.response.OrderResponse;
import com.example.Project2.entity.*;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    //Ham phu
    private Users findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy user: " + username));
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

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .shippingAddress(order.getShippingAddress())
                .phone(order.getPhone())
                .note(order.getNote())
                .paymentMethod(order.getPaymentMethod())
                .items(itemResponses)
                .build();
    }
    private String buildProductImageUrl(Product product) {
        if (product.getImageType() == null || product.getImageType().isBlank()) {
            return null;
        }
        return "/api/products/" + product.getId() + "/image";
    }


    // ─── Business Methods ────────────────────────────────────
    /**
     * POST /api/orders/checkout — Đặt hàng (COD hoặc VNPAY)
     *
     * Luồng:
     * 1. Lấy Cart + CartItems của user
     * 2. Kiểm tra giỏ không rỗng
     * 3. Kiểm tra đủ stock cho từng sản phẩm
     * 4. Tạo Order + OrderItems
     * 5. Trừ stock sản phẩm
     * 6. Xóa CartItems (clear giỏ hàng)
     * 7. Trả về OrderResponse
     *
     * Nếu paymentMethod = "VNPAY": status = AWAITING_PAYMENT, sinh vnpTxnRef
     * Nếu paymentMethod = "COD" (mặc định): status = PENDING
     */
    @Transactional
    public OrderResponse checkout(String username, OrderRequest request) {
        Users user = findUserByUsername(username);
        // 1. Lấy giỏ hàng
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppBadRequestException("Bạn chưa có giỏ hàng"));
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        // 2. Kiểm tra giỏ không rỗng
        if (cartItems.isEmpty()) {
            throw new AppBadRequestException("Giỏ hàng trống, không thể đặt hàng");
        }
        // 3. Kiểm tra stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new AppBadRequestException(
                        "Sản phẩm \"" + product.getName() + "\" chỉ còn " + product.getStock() + " sản phẩm");
            }
        }
        // 4. Tính tổng tiền
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Xác định payment method và status
        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "COD"; // mặc định
        }
        paymentMethod = paymentMethod.toUpperCase();

        OrderStatus initialStatus;
        String vnpTxnRef = null;
        if ("VNPAY".equals(paymentMethod)) {
            initialStatus = OrderStatus.AWAITING_PAYMENT;
            vnpTxnRef = VnPayConfig.getRandomNumber(8); // Mã 8 chữ số
        } else {
            initialStatus = OrderStatus.PENDING;
        }

        // 6. Tạo Order
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(initialStatus)
                .totalPrice(totalPrice)
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .note(request.getNote())
                .paymentMethod(paymentMethod)
                .vnpTxnRef(vnpTxnRef)
                .build();
        orderRepository.save(order);
        // 7. Tạo OrderItems + trừ stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice()) // Snapshot giá tại thời điểm mua
                    .build();
            orderItemRepository.save(orderItem);
            // Trừ stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        // 8. Xóa giỏ hàng
        cartItemRepository.deleteByCart(cart);
        // 9. Trả kết quả
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        return toOrderResponse(order, orderItems);
    }

    /**
     * GET /api/orders — Lấy danh sách đơn hàng của user
     */

    public List<OrderResponse> getMyOrders(String username){
        Users user = findUserByUsername(username);
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);

        return orders.stream().map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            return toOrderResponse(order , items);
        }).collect(Collectors.toList());
    }

    /**
     * GET /api/orders/{id} — Xem chi tiết 1 đơn hàng
     * Kiểm tra đơn hàng thuộc về user hiện tại
     */
    public OrderResponse getOrderDetail(String username, Integer orderId) {
        Users user = findUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy đơn hàng với ID: " + orderId));
        // Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppBadRequestException("Đơn hàng này không thuộc về bạn");
        }
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return toOrderResponse(order, items);
    }

    /**
     * PUT /api/orders/{id}/cancel — Hủy đơn hàng
     * Chỉ hủy được khi status = PENDING hoặc AWAITING_PAYMENT
     * Hoàn lại stock cho sản phẩm
     */
    @Transactional
    public OrderResponse cancelOrder(String username, Integer orderId) {
        Users user = findUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy đơn hàng với ID: " + orderId));
        // Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppBadRequestException("Đơn hàng này không thuộc về bạn");
        }
        // Chỉ hủy được khi PENDING hoặc AWAITING_PAYMENT
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new AppBadRequestException(
                    "Không thể hủy đơn hàng ở trạng thái: " + order.getStatus().name());
        }
        // Hoàn lại stock
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        // Cập nhật trạng thái
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return toOrderResponse(order, items);
    }

    /**
     * Tìm Order theo ID (dùng cho PaymentController)
     */
    public Order findOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    /**
     * Tìm Order theo vnpTxnRef (dùng cho VNPay callback)
     */
    public Order findByVnpTxnRef(String vnpTxnRef) {
        return orderRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy đơn hàng với mã giao dịch: " + vnpTxnRef));
    }
}
