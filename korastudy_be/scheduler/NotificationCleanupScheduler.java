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
     * Xóa thông báo hết hạn hàng ngày vào lúc 1 giờ sáng
     */
    @Scheduled(cron = "0 0 1 * * ?") // Chạy lúc 1:00 AM mỗi ngày
    @Transactional
    public void cleanupExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = notificationRepository.deleteByExpirationDateBefore(now);
        log.info("Đã xóa {} thông báo hết hạn", deletedCount);
    }
}