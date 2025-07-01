package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.LoginRequest;
import korastudy.be.dto.request.RegisterRequest;
import korastudy.be.dto.response.JwtResponse;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IAccountService;
import korastudy.be.service.INotificationService;
import korastudy.be.service.IRoleService;
import korastudy.be.service.IUserService;
import korastudy.be.service.impl.RoleService;
import korastudy.be.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAccountService accountService;
    private final IRoleService roleService;
    private final IUserService userService;
    private final INotificationService notificationService;

    /*
    ThienTDV - Đăng ký và đăng nhập
     */


    //Người dùng đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<ApiSuccess> register(@Valid @RequestBody RegisterRequest request) {
        accountService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of("Đăng ký tài khoản thành công"));
    }


    //Admin tự thêm tài khoản cho các role quản lý


    // Chức năng login
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(accountService.login(request));
    }


}
