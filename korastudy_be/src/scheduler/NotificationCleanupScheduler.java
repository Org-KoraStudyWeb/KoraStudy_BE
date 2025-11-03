package korastudy.be.scheduler;

import korastudy.be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupScheduler {

    private final NotificationRepository notificationRepository;

    /**
     * Xóa thông báo hết hạn mỗi 15 phút
     * Thay đổi lịch chạy từ mỗi ngày sang mỗi 15 phút vì thông báo giờ hết hạn nhanh hơn
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // Chạy mỗi 15 phút
    @Transactional
    public void cleanupExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = notificationRepository.deleteByExpirationDateBefore(now);
        log.info("Đã xóa {} thông báo hết hạn", deletedCount);
    }
}