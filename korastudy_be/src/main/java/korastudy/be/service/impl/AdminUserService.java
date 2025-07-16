package korastudy.be.service.impl;

import korastudy.be.dto.request.auth.AdminUpdateUserRequest;
import korastudy.be.dto.response.auth.UserManagementResponse;
import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.Notification;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.exception.AccountException;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.NotificationRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService implements IAdminUserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserManagementResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsersList() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserManagementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUserCodeContainingIgnoreCase(
                keyword, keyword, keyword, keyword, pageable);
        return users.map(UserManagementResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getUsersByRole(String roleName, Pageable pageable) {
        try {
            RoleName role = RoleName.valueOf(roleName.toUpperCase());
            Page<User> users = userRepository.findByAccount_Roles_RoleName(role, pageable);
            return users.map(UserManagementResponse::fromEntity);
        } catch (IllegalArgumentException e) {
            throw new AccountException("Role không hợp lệ: " + roleName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getUsersByStatus(Boolean isEnabled, Pageable pageable) {
        Page<User> users = userRepository.findByIsEnable(isEnabled, pageable);
        return users.map(UserManagementResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserManagementResponse getUserDetail(Long userId) {
        User user = getUserById(userId);
        return UserManagementResponse.fromEntity(user);
    }

    @Override
    public void updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = getUserById(userId);

        // Cập nhật thông tin user
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getDateOfBirth() != null) user.setDob(request.getDateOfBirth());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getLevel() != null) user.setLevel(request.getLevel());
        if (request.getIdCard() != null) user.setIdCard(request.getIdCard());

        // Cập nhật account info
        if (user.getAccount() != null) {
            Account account = user.getAccount();
            
            // Update email trong account nếu có thay đổi
            if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
                if (accountRepository.existsByEmailAndIdNot(request.getEmail(), account.getId())) {
                    throw new AccountException("Email đã được sử dụng bởi tài khoản khác");
                }
                account.setEmail(request.getEmail());
            }

            // Update username nếu có
            if (request.getUsername() != null && !request.getUsername().equals(account.getUsername())) {
                if (accountRepository.existsByUsernameAndIdNot(request.getUsername(), account.getId())) {
                    throw new AccountException("Username đã được sử dụng bởi tài khoản khác");
                }
                account.setUsername(request.getUsername());
            }

            accountRepository.save(account);
        }

        userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(Long userId, Boolean enabled) {
        User user = getUserById(userId);
        user.setEnable(enabled);
        
        // Cũng update account status
        if (user.getAccount() != null) {
            user.getAccount().setEnabled(enabled);
            accountRepository.save(user.getAccount());
        }
        
        userRepository.save(user);

        // Gửi notification cho user
        String message = enabled ? "Tài khoản của bạn đã được kích hoạt" : "Tài khoản của bạn đã bị vô hiệu hóa";
        sendNotificationToUser(user, "Cập nhật trạng thái tài khoản", message);
    }

    @Override
    public void resetUserPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        
        if (user.getAccount() == null) {
            throw new AccountException("User không có account để đặt lại mật khẩu");
        }

        // Đặt lại mật khẩu
        user.getAccount().setEncryptedPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(user.getAccount());

        // Gửi notification
        sendNotificationToUser(user, "Mật khẩu đã được đặt lại", 
                "Admin đã đặt lại mật khẩu cho tài khoản của bạn. Vui lòng đăng nhập bằng mật khẩu mới.");
    }

    @Override
    public void approveUserProfile(Long userId) {
        User user = getUserById(userId);
        user.setEnable(true);
        userRepository.save(user);

        // Gửi notification approval
        sendNotificationToUser(user, "Hồ sơ được phê duyệt", 
                "Hồ sơ của bạn đã được phê duyệt thành công.");
    }

    @Override
    public void rejectUserProfile(Long userId, String reason) {
        User user = getUserById(userId);
        user.setEnable(false);
        userRepository.save(user);

        // Gửi notification từ chối
        sendNotificationToUser(user, "Hồ sơ bị từ chối", 
                "Hồ sơ của bạn đã bị từ chối. Lý do: " + reason + ". Vui lòng cập nhật lại thông tin.");
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        
        // Soft delete: set isEnable = false
        user.setEnable(false);
        if (user.getAccount() != null) {
            user.getAccount().setEnabled(false);
            accountRepository.save(user.getAccount());
        }
        
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Thống kê tổng quan
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsEnableTrue();
        long inactiveUsers = totalUsers - activeUsers;

        // Thống kê theo role
        long admins = userRepository.countByAccount_Roles_RoleName(RoleName.ADMIN);
        long contentManagers = userRepository.countByAccount_Roles_RoleName(RoleName.CONTENT_MANAGER);
        long deliveryManagers = userRepository.countByAccount_Roles_RoleName(RoleName.DELIVERY_MANAGER);
        long regularUsers = userRepository.countByAccount_Roles_RoleName(RoleName.USER);

        stats.put("overview", Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "inactiveUsers", inactiveUsers
        ));

        stats.put("byRole", Map.of(
                "admins", admins,
                "contentManagers", contentManagers,
                "deliveryManagers", deliveryManagers,
                "regularUsers", regularUsers
        ));

        return stats;
    }

    // Helper methods
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));
    }

    private void sendNotificationToUser(User user, String title, String content) {
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .isPublished(true)
                .user(user)
                .build();
        notificationRepository.save(notification);
    }
}