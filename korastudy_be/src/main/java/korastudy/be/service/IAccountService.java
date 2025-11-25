package korastudy.be.service;

import korastudy.be.dto.request.auth.CreateAccountRequest;
import korastudy.be.dto.request.auth.LoginRequest;
import korastudy.be.dto.request.auth.RegisterRequest;
import korastudy.be.dto.response.auth.JwtResponse;

public interface IAccountService {

    //Đăng ký tài khoản
    void register(RegisterRequest request);

    //Admin thêm tài khoản
    void createInternalAccount(CreateAccountRequest request);

    // Đăng nhập, trả token + roles + account info
    JwtResponse login(LoginRequest loginRequest);

    //Kích hoạt hoặc khóa tài khoản
    void enableAccount(Long accountId, boolean enable);

    // Dùng khi người dùng tự đổi mật khẩu (yêu cầu xác thực mật khẩu cũ)
    void changePassword(String username, String oldPassword, String newPassword);

    // Dùng cho admin đặt lại mật khẩu cho ai đó
    void resetPasswordByAdmin(Long accountId, String newPassword);

    // Quên mật khẩu
    void forgotPassword(String email);

    // Reset mật khẩu với token
    void resetPassword(String token, String newPassword);

    // Validate reset token
    void validateResetToken(String token);


}
