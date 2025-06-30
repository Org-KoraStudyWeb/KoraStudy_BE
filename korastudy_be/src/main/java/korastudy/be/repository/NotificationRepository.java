package korastudy.be.repository;


import korastudy.be.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /*
    ThienTDV
    */

    //Lấy tất cả notification của 1 user nào đó
    List<Notification> findByUserId(Long userId);

    //Lọc theo isPublished
    List<Notification> findByIsPublished(boolean isPublished);

    //Tìm tất cả thông báo chưa đọc của một user
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isPublished = false")
    List<Notification> findUnpublishedByUserId(@Param("userId") Long userId);

    List<Notification> findAllByUser_Id(Long userId);
}
