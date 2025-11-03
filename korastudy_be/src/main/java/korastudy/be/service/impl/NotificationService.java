package korastudy.be.service.impl;

import korastudy.be.dto.request.notification.SystemNotificationRequest;
import korastudy.be.dto.response.notification.NotificationResponse;
import korastudy.be.dto.response.notification.RealTimeNotificationResponse;
import korastudy.be.entity.Enum.NotificationType;
import korastudy.be.entity.Notification;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.NotificationRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket messaging template

    @Override
    public void notifyProfileRequired(User user, String roleName) {
        Notification notification = Notification.builder()
                .title("Yêu cầu cập nhật hồ sơ")
                .content("Bạn đã được cấp tài khoản với quyền " + roleName + ". Vui lòng cập nhật hồ sơ.")
                .read(false)
                .user(user)
                .type(NotificationType.PROFILE)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Gửi thông báo real-time
        sendRealTimeNotification(user, savedNotification);
    }

    @Override
    public void notifyProfileApproved(User user) {
        Notification notification = Notification.builder()
                .title("Hồ sơ đã được duyệt")
                .content("Hồ sơ cá nhân của bạn đã được admin phê duyệt.")
                .read(false)
                .user(user)
                .type(NotificationType.PROFILE)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Gửi thông báo real-time
        sendRealTimeNotification(user, savedNotification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findAllByUser_Id(userId);
    }
    
    @Override
    public List<NotificationResponse> getUserNotifications(Long userId) {
        // Chỉ lấy các thông báo chưa hết hạn
        List<Notification> notifications = notificationRepository.findActiveNotificationsByUserId(userId);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NotificationResponse> getUserNotificationsByType(Long userId, NotificationType type) {
        List<Notification> notifications = notificationRepository.findActiveNotificationsByUserIdAndType(userId, type);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Override
    public void sendSystemNotification(SystemNotificationRequest request) {
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            // Gửi cho tất cả người dùng
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                Notification notification = createNotification(user, request.getTitle(), 
                        request.getContent(), NotificationType.SYSTEM, null);
                
                // Gửi thông báo real-time
                sendRealTimeNotification(user, notification);
            }
        } else {
            // Gửi cho danh sách người dùng cụ thể
            for (Long userId : request.getUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
                
                Notification notification = createNotification(user, request.getTitle(), 
                        request.getContent(), NotificationType.SYSTEM, null);
                
                // Gửi thông báo real-time
                sendRealTimeNotification(user, notification);
            }
        }
    }
    
    @Override
    public int countUnreadNotifications(Long userId) {
        // Chỉ đếm các thông báo chưa đọc và chưa hết hạn
        return notificationRepository.countUnreadActiveByUserId(userId);
    }
    
    @Override
    public int countUnreadNotificationsByType(Long userId, NotificationType type) {
        return notificationRepository.countUnreadActiveByUserIdAndType(userId, type);
    }
    
    @Override
    public void sendForumInteractionNotification(User recipient, String title, String content, Long postId) {
        Notification notification = createNotification(recipient, title, content, 
                NotificationType.FORUM_INTERACTION, postId);
        
        // Gửi thông báo real-time
        sendRealTimeNotification(recipient, notification);
    }
    
    @Override
    public void sendExamResultNotification(User recipient, String title, String content, Long examId) {
        Notification notification = createNotification(recipient, title, content, 
                NotificationType.EXAM_RESULT, examId);
        
        // Gửi thông báo real-time
        sendRealTimeNotification(recipient, notification);
    }
    
    // Helper method để tạo notification
    private Notification createNotification(User user, String title, String content, 
                                           NotificationType type, Long referenceId) {
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .read(false)
                .user(user)
                .type(type)
                .referenceId(referenceId)
                .build();
        return notificationRepository.save(notification);
    }
    
    // Helper method để gửi thông báo real-time
    private void sendRealTimeNotification(User user, Notification notification) {
        // Chuyển notification sang DTO phù hợp cho real-time
        RealTimeNotificationResponse response = RealTimeNotificationResponse.fromEntity(notification);
        
        // Gửi thông báo đến client thông qua WebSocket
        // /user/{username}/queue/notifications là destination
        messagingTemplate.convertAndSendToUser(
                user.getAccount().getUsername(), 
                "/queue/notifications", 
                response
        );
    }
}