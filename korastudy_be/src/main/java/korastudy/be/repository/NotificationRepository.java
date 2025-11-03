package korastudy.be.repository;

import korastudy.be.entity.Enum.NotificationType;
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
    
    // Tìm kiếm thông báo theo loại
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.type = :type AND n.expirationDate > CURRENT_TIMESTAMP ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsByUserIdAndType(Long userId, NotificationType type);
    
    // Xóa thông báo hết hạn
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expirationDate < :dateTime")
    int deleteByExpirationDateBefore(LocalDateTime dateTime);
    
    // Xóa thông báo có expirationDate NULL nhưng đã tạo quá lâu (fallback an toàn)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expirationDate IS NULL AND n.createdAt < :threshold")
    int deleteWithNullExpirationAndCreatedBefore(LocalDateTime threshold);
    
    // Đếm số thông báo chưa đọc và chưa hết hạn
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false AND n.expirationDate > CURRENT_TIMESTAMP")
    int countUnreadActiveByUserId(Long userId);
    
    // Đếm số thông báo chưa đọc theo loại
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.type = :type AND n.read = false AND n.expirationDate > CURRENT_TIMESTAMP")
    int countUnreadActiveByUserIdAndType(Long userId, NotificationType type);
}