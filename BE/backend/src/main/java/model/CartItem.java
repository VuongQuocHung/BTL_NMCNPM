package model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;

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

    @Transient
    private BigDecimal unitPrice;

    @Transient
    @Builder.Default
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Tinh thanh tien cho dung mot dong gio hang theo so do tuan tu:
     * CartItem.calculateLineTotal() = don gia hien tai cua Product * so luong.
     */
    public BigDecimal calculateLineTotal() {
        BigDecimal currentUnitPrice = unitPrice;
        if (currentUnitPrice == null && product != null) {
            currentUnitPrice = product.getPrice();
        }
        if (currentUnitPrice == null || quantity == null) {
            lineTotal = BigDecimal.ZERO;
            return lineTotal;
        }
        lineTotal = currentUnitPrice.multiply(BigDecimal.valueOf(quantity));
        return lineTotal;
    }
}
