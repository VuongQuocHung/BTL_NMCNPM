package model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {

    @NotNull(message = "San pham bat buoc phai co")
    private Long productId;

    @Min(value = 1, message = "So luong khong hop le")
    private Integer quantity = 1;
}
