package korastudy.be.repository;

import korastudy.be.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUser_Id(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    List<Notification> findUnreadByUserId(Long userId);
    
    // Tìm kiếm thông báo chưa hết hạn
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.expirationDate > CURRENT_TIMESTAMP ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsByUserId(Long userId);
    
    // Xóa thông báo hết hạn
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expirationDate < :dateTime")
    int deleteByExpirationDateBefore(LocalDateTime dateTime);
    
    // Đếm số thông báo chưa đọc và chưa hết hạn
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false AND n.expirationDate > CURRENT_TIMESTAMP")
    int countUnreadActiveByUserId(Long userId);
}