package korastudy.be.service;

import korastudy.be.entity.Notification;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationService {
    void notifyProfileRequired(User user, String roleName);// gửi noti khi manager cần điền hồ sơ

    void notifyProfileApproved(User user);

    List<Notification> getNotificationsForUser(Long userId);
}
