package com.ttcs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_cart_product", columnNames = {"cart_id", "product_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
     * UC-2.3/UC-2.4: CartItem chịu trách nhiệm đổi số lượng và tính thành tiền
     * theo đơn giá hiện tại của Product, không khóa giá tại thời điểm thêm vào giỏ.
     */
    public void increaseQuantity(int amount) {
        updateQuantity((quantity == null ? 0 : quantity) + amount);
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Số lượng sản phẩm trong giỏ hàng tối thiểu là 1");
        }
        quantity = newQuantity;
    }

    public BigDecimal calculateLineTotal() {
        BigDecimal unitPrice = product == null ? BigDecimal.ZERO : product.getPrice();
        return calculateLineTotal(unitPrice, quantity == null ? 0 : quantity);
    }

    public BigDecimal calculateLineTotal(BigDecimal unitPrice) {
        return calculateLineTotal(unitPrice, quantity == null ? 0 : quantity);
    }

    public static BigDecimal calculateLineTotal(BigDecimal unitPrice, int quantity) {
        BigDecimal safePrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        int safeQuantity = Math.max(0, quantity);
        return safePrice.multiply(BigDecimal.valueOf(safeQuantity));
    }
}
