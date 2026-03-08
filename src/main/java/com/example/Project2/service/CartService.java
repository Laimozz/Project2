package com.example.Project2.service;

import com.example.Project2.dto.request.CartItemRequest;
import com.example.Project2.dto.response.CartItemResponse;
import com.example.Project2.dto.response.CartResponse;
import com.example.Project2.entity.Cart;
import com.example.Project2.entity.CartItem;
import com.example.Project2.entity.Product;
import com.example.Project2.entity.Users;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.CartItemRepository;
import com.example.Project2.repository.CartRepository;
import com.example.Project2.repository.ProductRepository;
import com.example.Project2.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;


    private Users findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy user: " + username));
    }

    private Cart getOrCreateCart(Users user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    // Tạo giỏ hàng mới cho user lần đầu
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(item.getProduct().getPrice())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .build();
    }

    private CartResponse toCartResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse).collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }


    //Business Method

    /**
     * GET /api/cart — Xem giỏ hàng của user hiện tại.
     * Nếu user chưa có giỏ → tạo giỏ rỗng và trả về.
     *
     * @param username tên đăng nhập (lấy từ SecurityContext)
     * @return CartResponse chứa danh sách items và tổng tiền
     */
    public CartResponse getCart(String username){
        Users user = findUserByUsername(username);
        Cart cart = getOrCreateCart(user);
        List<CartItem> items = cartItemRepository.findByCart(cart);
        return toCartResponse(cart, items);
    }

    /**
     * POST /api/cart/items — Thêm sản phẩm vào giỏ hàng.
     * Nếu sản phẩm đã có trong giỏ → cộng dồn số lượng.
     * Nếu chưa có → tạo CartItem mới.
     *
     * @param username tên đăng nhập
     * @param request  chứa productId và quantity
     * @return CartResponse sau khi cập nhật
     */
    public CartResponse addItem(String username, CartItemRequest request) {
        Users user = findUserByUsername(username);
        Cart cart = getOrCreateCart(user);

        // Tìm sản phẩm theo ID
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppBadRequestException(
                        "Sản phẩm không tồn tại với ID: " + request.getProductId()));

        cartItemRepository.findByCartAndProduct(cart, product).ifPresentOrElse(
                existingItem -> {
                    // Đã có → cộng dồn quantity
                    existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                    cartItemRepository.save(existingItem);
                }, () -> {
                    // Chưa có → tạo CartItem mới
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(request.getQuantity())
                            .build();
                    cartItemRepository.save(newItem);
                });
        List<CartItem> items = cartItemRepository.findByCart(cart);
        return toCartResponse(cart, items);
    }

    /**
     * PUT /api/cart/items/{itemId} — Cập nhật số lượng sản phẩm trong giỏ.
     * Chỉ cho phép sửa CartItem thuộc giỏ của chính user đó.
     *
     * @param username tên đăng nhập
     * @param itemId   ID của CartItem cần cập nhật
     * @param request  chứa quantity mới
     * @return CartResponse sau khi cập nhật
     */
    public CartResponse updateItemQuantity(String username, Integer itemId, CartItemRequest request) {

        Users user = findUserByUsername(username);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("NOT FOUND"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AppBadRequestException("Item này không thuộc giỏ hàng của bạn");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        List<CartItem> items = cartItemRepository.findByCart(cart);
        return toCartResponse(cart , items);
    }

    /**
     * DELETE /api/cart/items/{itemId} — Xóa 1 sản phẩm khỏi giỏ hàng.
     * Chỉ cho phép xóa CartItem thuộc giỏ của chính user đó.
     *
     * @param username tên đăng nhập
     * @param itemId   ID của CartItem cần xóa
     * @return Map chứa message thành công
     */
    public Map<String , String> removeItem(String username , Integer itemId){
        Users user = findUserByUsername(username);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("MOT FOUDN"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AppBadRequestException("Item này không thuộc giỏ hàng của bạn");
        }

        cartItemRepository.delete(cartItem);
        return Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng");
    }

    /**
     * DELETE /api/cart — Xóa toàn bộ sản phẩm trong giỏ hàng.
     * Chỉ xóa các CartItem, giữ lại Cart entity.
     *
     * @param username tên đăng nhập
     * @return Map chứa message thành công
     */
    @Transactional
    public Map<String , String> clearCart(String username){
        Users users = findUserByUsername(username);
        Cart cart = getOrCreateCart(users);

        cartItemRepository.deleteByCart(cart);
        return Map.of("message", "Đã xóa toàn bộ giỏ hàng");
    }
}
