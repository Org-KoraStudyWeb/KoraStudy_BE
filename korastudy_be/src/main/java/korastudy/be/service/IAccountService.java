package korastudy.be.service;

import korastudy.be.dto.request.CreateAccountRequest;
import korastudy.be.dto.request.LoginRequest;
import korastudy.be.dto.request.RegisterRequest;
import korastudy.be.dto.response.JwtResponse;

public interface IAccountService {

    //Đăng ký tài khoản
    void register(RegisterRequest request);

    //Admin thêm tài khoản
    void createInternalAccount(CreateAccountRequest request);

    // Đăng nhập, trả token + roles + account info
    JwtResponse login(LoginRequest loginRequest);

    //Kiểm tra role để Redirect phù hợp
    String resolveHomePageByRole(String userName);

    //Kích hoạt hoặc khóa tài khoản
    void enableAccount(Long accountId, boolean enable);

    //Gán role cho account
    void assignRole(Long accountId, String roleName);

    // Dùng khi người dùng tự đổi mật khẩu (yêu cầu xác thực mật khẩu cũ)
    void changePassword(String username, String oldPassword, String newPassword);

    // Dùng cho admin đặt lại mật khẩu cho ai đó
    void resetPasswordByAdmin(Long accountId, String newPassword);


}
