package korastudy.be.controller;

import korastudy.be.dto.request.auth.AdminUpdateUserRequest;
import korastudy.be.dto.response.auth.UserManagementResponse;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final IAdminUserService adminUserService;

    /**
     * Lấy danh sách tất cả users (có phân trang)
     */
    @GetMapping
    public ResponseEntity<Page<UserManagementResponse>> getAllUsers(Pageable pageable) {
        Page<UserManagementResponse> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Lấy danh sách users không phân trang (cho dropdown, export...)
     */
    @GetMapping("/list")
    public ResponseEntity<List<UserManagementResponse>> getAllUsersList() {
        List<UserManagementResponse> users = adminUserService.getAllUsersList();
        return ResponseEntity.ok(users);
    }

    /**
     * Tìm kiếm users theo keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserManagementResponse>> searchUsers(
            @RequestParam String keyword, 
            Pageable pageable) {
        Page<UserManagementResponse> users = adminUserService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Lọc users theo role
     */
    @GetMapping("/by-role")
    public ResponseEntity<Page<UserManagementResponse>> getUsersByRole(
            @RequestParam String roleName, 
            Pageable pageable) {
        Page<UserManagementResponse> users = adminUserService.getUsersByRole(roleName, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Lọc users theo trạng thái enable/disable
     */
    @GetMapping("/by-status")
    public ResponseEntity<Page<UserManagementResponse>> getUsersByStatus(
            @RequestParam Boolean isEnabled, 
            Pageable pageable) {
        Page<UserManagementResponse> users = adminUserService.getUsersByStatus(isEnabled, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Xem chi tiết user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserManagementResponse> getUserDetail(@PathVariable Long userId) {
        UserManagementResponse user = adminUserService.getUserDetail(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Cập nhật thông tin user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiSuccess> updateUser(
            @PathVariable Long userId, 
            @Valid @RequestBody AdminUpdateUserRequest request) {
        adminUserService.updateUser(userId, request);
        return ResponseEntity.ok(ApiSuccess.of("Cập nhật thông tin user thành công"));
    }

    /**
     * Kích hoạt/vô hiệu hóa user
     */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiSuccess> toggleUserStatus(
            @PathVariable Long userId, 
            @RequestParam Boolean enabled) {
        adminUserService.toggleUserStatus(userId, enabled);
        String message = enabled ? "Đã kích hoạt user" : "Đã vô hiệu hóa user";
        return ResponseEntity.ok(ApiSuccess.of(message));
    }

    /**
     * Đặt lại mật khẩu cho user
     */
    @PutMapping("/{userId}/reset-password")
    public ResponseEntity<ApiSuccess> resetUserPassword(
            @PathVariable Long userId, 
            @RequestParam String newPassword) {
        adminUserService.resetUserPassword(userId, newPassword);
        return ResponseEntity.ok(ApiSuccess.of("Đặt lại mật khẩu thành công"));
    }

    /**
     * Phê duyệt hồ sơ user (cho manager profiles)
     */
    @PatchMapping("/{userId}/approve-profile")
    public ResponseEntity<ApiSuccess> approveUserProfile(@PathVariable Long userId) {
        adminUserService.approveUserProfile(userId);
        return ResponseEntity.ok(ApiSuccess.of("Đã phê duyệt hồ sơ user"));
    }

    /**
     * Từ chối hồ sơ user
     */
    @PatchMapping("/{userId}/reject-profile")
    public ResponseEntity<ApiSuccess> rejectUserProfile(
            @PathVariable Long userId, 
            @RequestParam String reason) {
        adminUserService.rejectUserProfile(userId, reason);
        return ResponseEntity.ok(ApiSuccess.of("Đã từ chối hồ sơ user"));
    }

    /**
     * Xóa user (soft delete)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccess> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(ApiSuccess.of("Đã xóa user thành công"));
    }

    /**
     * Thống kê users
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getUserStatistics() {
        Object stats = adminUserService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
}