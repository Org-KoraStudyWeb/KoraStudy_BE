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
     * Xóa thông báo hết hạn mỗi 15 phút.
     * Đồng thời dọn các thông báo (do lỗi tạo) không có expirationDate nhưng đã quá 24 giờ.
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // Chạy mỗi 15 phút
    @Transactional
    public void cleanupExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        int deletedExpired = notificationRepository.deleteByExpirationDateBefore(now);
        // Fallback: nếu có dòng bị NULL expiration_date (do migrate hay bug), dọn sau 24h
        int deletedNullExp = notificationRepository.deleteWithNullExpirationAndCreatedBefore(now.minusHours(24));
        int total = deletedExpired + deletedNullExp;
        if (total > 0) {
            log.info("Đã xóa {} thông báo (hết hạn: {}, null-exp: {})", total, deletedExpired, deletedNullExp);
        }
    }
}
