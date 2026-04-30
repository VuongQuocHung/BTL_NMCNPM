package com.ttcs.backend.controller;

import com.ttcs.backend.cart.dto.ApplyVoucherRequest;
import com.ttcs.backend.cart.dto.CartItemRequest;
import com.ttcs.backend.cart.dto.CartResponse;
import com.ttcs.backend.cart.dto.UpdateCartItemRequest;
import com.ttcs.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API", description = "Quản lý giỏ hàng theo session hoặc database")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem chi tiết giỏ hàng")
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getCurrentCart(request));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest body,
                                                HttpServletRequest request) {
        return ResponseEntity.ok(cartService.addItem(body.getProductId(), body.getQuantity(), request));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId,
                                                   @Valid @RequestBody UpdateCartItemRequest body,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(cartService.updateItem(productId, body.getQuantity(), request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(cartService.removeItem(productId, request));
    }

    @DeleteMapping("/items")
    @Operation(summary = "Xóa tất cả sản phẩm trong giỏ hàng")
    public ResponseEntity<CartResponse> clearCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.clearCart(request));
    }

    @PostMapping("/voucher")
    @Operation(summary = "Ap dung voucher vao gio hang")
    public ResponseEntity<CartResponse> applyVoucher(@Valid @RequestBody ApplyVoucherRequest body,
                                                     HttpServletRequest request) {
        return ResponseEntity.ok(cartService.applyVoucher(body.getCode(), request));
    }

    @DeleteMapping("/voucher")
    @Operation(summary = "Go voucher khoi gio hang")
    public ResponseEntity<CartResponse> removeVoucher(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.removeVoucher(request));
    }
}
