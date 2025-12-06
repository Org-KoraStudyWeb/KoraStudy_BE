package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.auth.*;
import korastudy.be.dto.response.auth.JwtResponse;
import korastudy.be.exception.AccountException;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.impl.AccountService;
import korastudy.be.service.impl.NotificationService;
import korastudy.be.service.impl.RoleService;
import korastudy.be.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AccountService accountService;
    private final RoleService roleService;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * ThienTDV - Đăng ký và đăng nhập
     */


    //Người dùng đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<ApiSuccess> register(@Valid @RequestBody RegisterRequest request) {
        accountService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of("Đăng ký tài khoản thành công"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiSuccess> verifyEmail(@RequestParam String token) {
        try {
            accountService.verifyEmail(token);
            return ResponseEntity.ok(ApiSuccess.of("Email đã được xác thực thành công!"));

        } catch (AccountException e) {
            // Chỉ throw exception cho các lỗi thực sự (token hết hạn)
            throw e;
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiSuccess> resendVerificationEmail(@RequestParam String email) {
        accountService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiSuccess.of("Đã gửi lại email xác thực"));
    }

    // Chức năng login
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(accountService.login(request));
    }

    //*************************CHỨC NĂNG CHƯA TEST****************************************
    //Admin tự thêm tài khoản cho các role quản lý
    @PostMapping("/createAccount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiSuccess> createInternalAccount(@Valid @RequestBody CreateAccountRequest request) {
        accountService.createInternalAccount(request);
        return ResponseEntity.ok(ApiSuccess.of("Tạo tài khoản thành công"));
    }

    //Chức năng kích hoạt hoặc khóa tài khoản (bật/tắt enable)
    @PatchMapping("/{accountId}/enable")
    public ResponseEntity<ApiSuccess> enableAccount(@PathVariable long accountId, @RequestParam boolean enable) {
        accountService.enableAccount(accountId, enable);
        return ResponseEntity.ok(ApiSuccess.of(enable ? "Tài khoản đã được kích hoạt" : "Tài khoản đã bị vô hiệu hóa"));
    }

    //thay đổi mật khẩu
    @PutMapping("/change-password")
    public ResponseEntity<ApiSuccess> changePassword(@AuthenticationPrincipal(expression = "username") String username, @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiSuccess.of("Đổi mật khẩu thành công"));
    }


    //Admin đặt lại mật khẩu cho người dùng bất kỳ
    @PutMapping("/{accountId}/reset-password")
    public ResponseEntity<ApiSuccess> resetPassword(@PathVariable Long accountId, @RequestBody ResetPasswordRequest request) {
        accountService.resetPasswordByAdmin(accountId, request.getPassword());
        return ResponseEntity.ok(ApiSuccess.of("Đặt lại mật khẩu thành công"));
    }

    // Quên mật khẩu - Gửi email reset
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiSuccess> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        accountService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiSuccess.of("Đã gửi link đặt lại mật khẩu đến email của bạn"));
    }

    // Reset mật khẩu với token
    @PostMapping("/reset-password")
    public ResponseEntity<ApiSuccess> resetPassword(@Valid @RequestBody PasswordResetRequest request) {

        // 🔐 Xác thực token trước khi reset
        accountService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiSuccess.of("Đặt lại mật khẩu thành công"));
    }

    // Kiểm tra token reset có hợp lệ không
    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiSuccess> validateResetToken(@RequestParam String token) {
        accountService.validateResetToken(token);
        return ResponseEntity.ok(ApiSuccess.of("Token hợp lệ"));
    }

}


