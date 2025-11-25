package korastudy.be.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// Dùng cho người dùng quên mật khẩu
public class PasswordResetRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;  // 🔐 BẮT BUỘC

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String newPassword;  // Nên đổi từ 'password' -> 'newPassword'
}
