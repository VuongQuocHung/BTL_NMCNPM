package com.ttcs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "applied_voucher_code", length = 50)
    private String appliedVoucherCode;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
     * UC-2.x: Cart luôn là nơi quản lý danh sách CartItem.
     * Service/Controller chỉ điều phối request, còn thao tác tìm, thêm, xóa và tính tổng
     * được gom ở model để bám đúng sơ đồ thiết kế.
     */
    public Optional<CartItem> findCartItem(Long productId) {
        if (productId == null || items == null) {
            return Optional.empty();
        }

        return items.stream()
                .filter(item -> item.getProduct() != null && productId.equals(item.getProduct().getId()))
                .findFirst();
    }

    public CartItem addProduct(Product product) {
        return addProduct(product, 1);
    }

    public CartItem addProduct(Product product, int quantity) {
        CartItem item = CartItem.builder()
                .product(product)
                .quantity(quantity)
                .build();
        addItem(item);
        return item;
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        if (item == null) {
            return;
        }
        items.remove(item);
        item.setCart(null);
    }

    public boolean removeItemByProductId(Long productId) {
        Optional<CartItem> item = findCartItem(productId);
        item.ifPresent(this::removeItem);
        return item.isPresent();
    }

    public void clearItems() {
        if (items != null) {
            items.forEach(item -> item.setCart(null));
            items.clear();
        }
        appliedVoucherCode = null;
    }

    public CartTotals calculateTotal() {
        return calculateTotal(BigDecimal.ZERO);
    }

    public CartTotals calculateTotal(BigDecimal discountAmount) {
        BigDecimal safeDiscount = discountAmount == null ? BigDecimal.ZERO : discountAmount.max(BigDecimal.ZERO);
        BigDecimal subtotal = items == null
                ? BigDecimal.ZERO
                : items.stream()
                .map(CartItem::calculateLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalQuantity = items == null
                ? 0
                : items.stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum();

        return new CartTotals(totalQuantity, subtotal, safeDiscount, subtotal.subtract(safeDiscount).max(BigDecimal.ZERO));
    }

    @Getter
    @AllArgsConstructor
    public static class CartTotals {
        private int totalQuantity;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
    }
}
