package korastudy.be.service;

import korastudy.be.dto.request.auth.AdminUpdateUserRequest;
import korastudy.be.dto.response.auth.UserManagementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IAdminUserService {

    // Lấy danh sách users
    Page<UserManagementResponse> getAllUsers(Pageable pageable);
    List<UserManagementResponse> getAllUsersList();

    // Tìm kiếm và lọc
    Page<UserManagementResponse> searchUsers(String keyword, Pageable pageable);
    Page<UserManagementResponse> getUsersByRole(String roleName, Pageable pageable);
    Page<UserManagementResponse> getUsersByStatus(Boolean isEnabled, Pageable pageable);

    // CRUD operations
    UserManagementResponse getUserDetail(Long userId);
    void updateUser(Long userId, AdminUpdateUserRequest request);
    void deleteUser(Long userId);

    // User management actions
    void toggleUserStatus(Long userId, Boolean enabled);
    void resetUserPassword(Long userId, String newPassword);
    void approveUserProfile(Long userId);
    void rejectUserProfile(Long userId, String reason);

    // Statistics
    Map<String, Object> getUserStatistics();
}