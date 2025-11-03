package korastudy.be.controller;

import korastudy.be.dto.request.notification.SystemNotificationRequest;
import korastudy.be.dto.response.notification.NotificationResponse;
import korastudy.be.entity.Enum.NotificationType;
import korastudy.be.entity.Notification;
import korastudy.be.entity.User.User;
import korastudy.be.exception.AlreadyExistsException;
import korastudy.be.dto.response.ApiSuccess;
import korastudy.be.service.INotificationService;
import korastudy.be.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final INotificationService notificationService;
    private final UserService userService;

    /**
     * Lấy thông báo của người dùng đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByAccountUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy thông tin người dùng"));
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }
    
    /**
     * Lấy thông báo theo loại
     */
    @GetMapping("/me/type/{type}")
    public ResponseEntity<List<NotificationResponse>> getMyNotificationsByType(
            Principal principal,
            @PathVariable String type) {
        String username = principal.getName();
        User user = userService.getUserByAccountUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy thông tin người dùng"));
        
        NotificationType notificationType;
        try {
            notificationType = NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại thông báo không hợp lệ: " + type);
        }
        
        return ResponseEntity.ok(notificationService.getUserNotificationsByType(user.getId(), notificationType));
    }
    
    /**
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByAccountUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy thông tin người dùng"));
        int count = notificationService.countUnreadNotifications(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Đếm số thông báo chưa đọc theo loại
     */
    @GetMapping("/unread-count/type/{type}")
    public ResponseEntity<Map<String, Integer>> getUnreadCountByType(
            Principal principal,
            @PathVariable String type) {
        String username = principal.getName();
        User user = userService.getUserByAccountUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy thông tin người dùng"));
        
        NotificationType notificationType;
        try {
            notificationType = NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại thông báo không hợp lệ: " + type);
        }
        
        int count = notificationService.countUnreadNotificationsByType(user.getId(), notificationType);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiSuccess> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiSuccess.of("Đã đánh dấu thông báo là đã đọc"));
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiSuccess> markAllAsRead(Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByAccountUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy thông tin người dùng"));
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiSuccess.of("Đã đánh dấu tất cả thông báo là đã đọc"));
    }
    
    /**
     * Admin gửi thông báo hệ thống
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/system")
    public ResponseEntity<ApiSuccess> sendSystemNotification(@RequestBody SystemNotificationRequest request) {
        notificationService.sendSystemNotification(request);
        return ResponseEntity.ok(ApiSuccess.of("Đã gửi thông báo hệ thống thành công"));
    }
}