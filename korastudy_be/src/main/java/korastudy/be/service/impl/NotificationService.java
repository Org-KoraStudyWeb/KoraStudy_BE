package korastudy.be.service.impl;

import korastudy.be.entity.Notification;
import korastudy.be.entity.User.User;
import korastudy.be.repository.NotificationRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void notifyProfileRequired(User user, String roleName) {
        Notification notification = Notification.builder().title("Yêu cầu cập nhật hồ sơ").content("Bạn đã được cấp tài khoản với quyền " + roleName + ". Vui lòng cập nhật hồ sơ.").isPublished(false).user(user).build();
        notificationRepository.save(notification);
    }

    @Override
    public void notifyProfileApproved(User user) {
        Notification notification = Notification.builder().title("Hồ sơ đã được duyệt").content("Hồ sơ cá nhân của bạn đã được admin phê duyệt.").isPublished(true).user(user).build();
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findAllByUser_Id(userId);
    }
}
