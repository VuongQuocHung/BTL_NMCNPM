package model.dto;

import lombok.Builder;
import lombok.Getter;
import model.VoucherDiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VoucherEligibilityResponse {
    private String code;
    private String name;
    private String description;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime endDate;
    private boolean eligible;
    private String eligibilityMessage;
}
