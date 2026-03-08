package com.example.Project2.controller;

import com.example.Project2.dto.request.CartItemRequest;
import com.example.Project2.dto.response.CartResponse;
import com.example.Project2.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * GET /api/cart — Xem giỏ hàng của user hiện tại.
     * Nếu chưa có giỏ → tự tạo giỏ rỗng và trả về.
     */
    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(){
        String username = getCurrentUsername();
        return ResponseEntity.ok(cartService.getCart(username));
    }

    /**
     * POST /api/cart/items — Thêm sản phẩm vào giỏ hàng.
     * Nếu sản phẩm đã có → cộng dồn quantity.
     */
    @PostMapping("/cart/items")
    public ResponseEntity<CartResponse> addItem(@RequestBody CartItemRequest request){
        String username = getCurrentUsername();
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(username , request));
    }

    /**
     * PUT /api/cart/items/{itemId} — Cập nhật số lượng sản phẩm trong giỏ.
     *
     * Request Body: { "productId": 1, "quantity": 5 }
     * (productId trong body sẽ bị bỏ qua, chỉ dùng quantity)
     */
    @PutMapping("/cart/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Integer itemId,
            @Valid @RequestBody CartItemRequest request) {
        String username = getCurrentUsername();
        return ResponseEntity.ok(cartService.updateItemQuantity(username, itemId, request));
    }

    /**
     * DELETE /api/cart/items/{itemId} — Xóa 1 sản phẩm khỏi giỏ hàng.
     */
    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<Map<String , String>> removeItem(@PathVariable Integer itemId){
        String username = getCurrentUsername();
        return ResponseEntity.ok(cartService.removeItem(username , itemId));
    }

    /**
     * DELETE /api/cart — Xóa toàn bộ sản phẩm trong giỏ hàng.
     * Giữ lại Cart entity, chỉ xóa các CartItem bên trong.
     */
    @DeleteMapping("/cart")
    public ResponseEntity<Map<String , String>> clearCart(){
        String username = getCurrentUsername();
        return ResponseEntity.status(HttpStatus.OK).body(cartService.clearCart(username));
    }

}
