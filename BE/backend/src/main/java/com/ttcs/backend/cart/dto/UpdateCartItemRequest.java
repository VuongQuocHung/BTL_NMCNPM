package com.ttcs.backend.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {

    @NotNull(message = "So luong bat buoc phai co")
    @Min(value = 1, message = "So luong khong hop le")
    private Integer quantity;
}
