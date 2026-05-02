package model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Token khong duoc de trong")
    private String token;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Size(min = 6, message = "Mat khau moi phai co it nhat 6 ky tu")
    private String newPassword;
}
