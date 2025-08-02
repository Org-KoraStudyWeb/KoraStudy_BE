package korastudy.be.service;

import korastudy.be.dto.request.notification.SystemNotificationRequest;
import korastudy.be.dto.response.notification.NotificationResponse;
import korastudy.be.entity.Notification;
import korastudy.be.entity.User.User;

import java.util.List;

public interface INotificationService {
    // Các phương thức hiện có
    void notifyProfileRequired(User user, String roleName);
    void notifyProfileApproved(User user);
    List<Notification> getNotificationsForUser(Long userId);
    
    // Thêm các phương thức mới
    List<NotificationResponse> getUserNotifications(Long userId);
    
    // Đánh dấu thông báo đã đọc
    void markAsRead(Long notificationId);
    
    // Đánh dấu tất cả thông báo của user đã đọc
    void markAllAsRead(Long userId);
    
    // Admin gửi thông báo hệ thống
    void sendSystemNotification(SystemNotificationRequest request);
    
    // Đếm số thông báo chưa đọc của user
    int countUnreadNotifications(Long userId);
}