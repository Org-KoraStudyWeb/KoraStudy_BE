package korastudy.be.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import korastudy.be.entity.Enum.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String jobEmail;

    @NotNull(message = "Vai trò không được để trống")
    private RoleName roleName;

    @NotBlank(message = "Mã định danh (userCode) không được để trống")
    private String userCode;
}
