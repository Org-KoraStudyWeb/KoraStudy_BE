package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.auth.CreateAccountRequest;
import korastudy.be.entity.User.Role;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.impl.AccountService;
import korastudy.be.service.impl.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {
    private final AccountService accountService;
    private final RoleService roleService;

    /*
    ThienTDV - tạo tài khoản nội bộ cho nhân viên
     */

    //Chỉ dành cho admin tạo tài khoản nội bộ
    @PostMapping("/create")
    public ResponseEntity<ApiSuccess> createManagerAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
        accountService.createInternalAccount(createAccountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of("Tạo tài khoản quản lý thành công. Chờ nhân viên cập nhật hồ sơ."));
    }

    // Lấy toàn bộ Role (ít dùng)
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // Lấy Role dành riêng cho quản lý (delivery_manager, content_manager,...)
    @GetMapping("/roles/manager")
    public ResponseEntity<List<Role>> getManagerRoles() {
        return ResponseEntity.ok(roleService.getManagerRoles());
    }
}
