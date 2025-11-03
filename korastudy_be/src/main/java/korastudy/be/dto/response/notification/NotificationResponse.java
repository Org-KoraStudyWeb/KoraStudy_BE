package korastudy.be.dto.response.notification;

import korastudy.be.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.isRead()) // Đã sửa từ isPublished() sang isRead()
                .createdAt(notification.getCreatedAt())
                .build();
    }
}