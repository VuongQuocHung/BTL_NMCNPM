package model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer stock;
    private Integer quantity;
    private BigDecimal lineTotal;
    private boolean outOfStock;
    private boolean quantityAdjusted;
    private String warning;
}
