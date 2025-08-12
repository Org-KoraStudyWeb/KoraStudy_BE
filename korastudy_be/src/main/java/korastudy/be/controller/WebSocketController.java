package korastudy.be.controller;

import korastudy.be.dto.response.notification.RealTimeNotificationResponse;
import korastudy.be.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    
    /**
     * Xử lý khi người dùng kết nối WebSocket
     */
    @MessageMapping("/connect")
    public void handleConnect(Principal principal) {
        // Gửi thông báo đến người dùng
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/status",
                "Kết nối WebSocket thành công!"
        );
    }
    
    /**
     * Gửi thông báo đến người dùng cụ thể
     */
    public void sendNotification(String username, RealTimeNotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );
    }

    /**
     * Gửi thông báo đến tất cả người dùng
     */
    public void broadcastNotification(RealTimeNotificationResponse notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    @MessageMapping("/notifications/mark-read")
    public void markNotificationAsRead(@Payload Map<String, Long> payload, Principal principal) {
        Long notificationId = payload.get("notificationId");
        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
            
            // Thông báo client cập nhật trạng thái
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/notifications/status",
                    Map.of(
                        "action", "MARKED_READ",
                        "notificationId", notificationId
                    )
            );
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo của người dùng đã đọc
     */
    @MessageMapping("/notifications/mark-all-read")
    public void markAllNotificationsAsRead(@Payload Map<String, Long> payload, Principal principal) {
        Long userId = payload.get("userId");
        if (userId != null) {
            notificationService.markAllAsRead(userId);
            
            // Thông báo client cập nhật trạng thái
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/notifications/status",
                    Map.of("action", "MARKED_ALL_READ")
            );
        }
    }
    
    /**
     * Lấy số lượng thông báo chưa đọc
     */
    @MessageMapping("/notifications/count-unread")
    public void getUnreadNotificationCount(@Payload Map<String, Long> payload, Principal principal) {
        Long userId = payload.get("userId");
        if (userId != null) {
            int count = notificationService.countUnreadNotifications(userId);
            
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/notifications/count",
                    Map.of("unreadCount", count)
            );
        }
    }
}