package com.ttcs.backend.controller;

import com.ttcs.backend.cart.dto.ApplyVoucherRequest;
import com.ttcs.backend.cart.dto.CartItemRequest;
import com.ttcs.backend.cart.dto.CartResponse;
import com.ttcs.backend.cart.dto.UpdateCartItemRequest;
import com.ttcs.backend.cart.dto.VoucherEligibilityResponse;
import com.ttcs.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API", description = "Quản lý giỏ hàng theo session hoặc database")
public class CartController {

    private final CartService cartService;

    // UC-2.4: CartDetailView gọi API này để lấy danh sách CartItem và tổng tiền hiện tại.
    @GetMapping
    @Operation(summary = "Xem chi tiết giỏ hàng")
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getCurrentCart(request));
    }

    // UC-2.1: ProductListView gửi productId, hệ thống thêm mặc định 1 sản phẩm.
    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest body,
                                                HttpServletRequest request) {
        return ResponseEntity.ok(cartService.addItem(body.getProductId(), request));
    }

    // UC-2.3: CartDetailView gửi số lượng mới, service kiểm tra min=1 và tồn kho.
    @PutMapping("/items/{productId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId,
                                                   @Valid @RequestBody UpdateCartItemRequest body,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(cartService.updateItem(productId, body.getQuantity(), request));
    }

    // UC-2.2: Xóa đúng dòng sản phẩm đang tồn tại trong giỏ hàng.
    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(cartService.removeItem(productId, request));
    }

    // UC-2.2 alternative: Xóa toàn bộ giỏ hàng hiện tại.
    @DeleteMapping("/items")
    @Operation(summary = "Xóa tất cả sản phẩm trong giỏ hàng")
    public ResponseEntity<CartResponse> clearCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.clearCart(request));
    }

    // UC-2.5: ApplyVoucherView lấy voucher kèm trạng thái đủ điều kiện theo subtotal.
    @GetMapping("/vouchers")
    @Operation(summary = "Xem voucher và trạng thái đủ điều kiện theo tạm tính giỏ hàng")
    public ResponseEntity<List<VoucherEligibilityResponse>> getAvailableVouchers(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getAvailableVouchers(request));
    }

    // UC-2.5: Khách hàng đã đăng nhập áp dụng voucher hợp lệ vào giỏ hàng.
    @PostMapping("/voucher")
    @Operation(summary = "Áp dụng voucher vào giỏ hàng")
    public ResponseEntity<CartResponse> applyVoucher(@Valid @RequestBody ApplyVoucherRequest body,
                                                     HttpServletRequest request) {
        return ResponseEntity.ok(cartService.applyVoucher(body.getCode(), request));
    }

    @DeleteMapping("/voucher")
    @Operation(summary = "Gỡ voucher khỏi giỏ hàng")
    public ResponseEntity<CartResponse> removeVoucher(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.removeVoucher(request));
    }
}
