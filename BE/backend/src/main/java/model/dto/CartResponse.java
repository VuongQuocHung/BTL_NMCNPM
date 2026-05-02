package model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal subtotal;
    private String voucherCode;
    private String voucherName;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private boolean empty;
    private String storageType;
    private String message;
    private String voucherMessage;
}
