package com.ttcs.backend.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyVoucherRequest {
    @NotBlank(message = "Voucher code is required")
    private String code;
}
