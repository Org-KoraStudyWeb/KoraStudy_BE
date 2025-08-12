package korastudy.be.controller;

import korastudy.be.dto.response.notification.NotificationResponse;
import korastudy.be.entity.Enum.NotificationType;
import korastudy.be.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;
    
    /**
     * Lấy danh sách thông báo của người dùng
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@RequestParam Long userId) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Lấy danh sách thông báo theo loại
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @RequestParam Long userId,
            @PathVariable NotificationType type) {
        List<NotificationResponse> notifications = notificationService.getUserNotificationsByType(userId, type);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    @PutMapping("/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@RequestBody Map<String, Long> payload) {
        Long notificationId = payload.get("notificationId");
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Notification marked as read"));
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead(@RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "All notifications marked as read"));
    }
    
    /**
     * Lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/count-unread")
    public ResponseEntity<Map<String, Integer>> getUnreadNotificationCount(@RequestParam Long userId) {
        int count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}