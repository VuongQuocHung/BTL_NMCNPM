package com.ttcs.backend.cart.dto;

import com.ttcs.backend.entity.VoucherDiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VoucherEligibilityResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private boolean eligible;
    private String eligibilityMessage;
    private BigDecimal expectedDiscountAmount;
}
