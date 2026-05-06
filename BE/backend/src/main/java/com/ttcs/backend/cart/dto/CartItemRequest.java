package com.ttcs.backend.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {

    @NotNull(message = "San pham bat buoc phai co")
    private Long productId;
}
