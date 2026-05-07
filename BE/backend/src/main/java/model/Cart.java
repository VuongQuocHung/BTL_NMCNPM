package model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
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
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "applied_voucher_code", length = 50)
    private String appliedVoucherCode;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CartItem> items = new HashSet<>();

    @Transient
    private int totalItems;

    @Transient
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Transient
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Transient
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /**
     * Ham duoc dat ten theo so do tuan tu: Cart.findCartItem(productId).
     * Tim dong gio hang ung voi san pham de them, cap nhat hoac xoa.
     */
    public Optional<CartItem> findCartItem(Long productId) {
        if (productId == null || items == null) {
            return Optional.empty();
        }
        return items.stream()
                .filter(item -> item.getProduct() != null)
                .filter(item -> productId.equals(item.getProduct().getId()))
                .findFirst();
    }

    public void calculateTotal() {
        calculateTotal(null);
    }

    /**
     * Ham duoc dat ten theo so do tuan tu: Cart.calculateTotal(voucher).
     * Tinh lai tong so luong, tam tinh, tien giam va tong tien cuoi cung.
     */
    public void calculateTotal(Voucher voucher) {
        totalItems = 0;
        subtotal = BigDecimal.ZERO;

        if (items != null) {
            for (CartItem item : items) {
                subtotal = subtotal.add(item.calculateLineTotal());
                totalItems += item.getQuantity() != null ? item.getQuantity() : 0;
            }
        }

        discountAmount = calculateDiscount(voucher, subtotal);
        totalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal temporaryTotal) {
        if (voucher == null || temporaryTotal == null || temporaryTotal.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (voucher.getMinOrderAmount() != null && temporaryTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            BigDecimal discount = temporaryTotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
            if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                return voucher.getMaxDiscountAmount();
            }
            return discount;
        }
        return voucher.getDiscountValue().min(temporaryTotal);
    }
}
