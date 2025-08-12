package korastudy.be.dto.response.notification;

import korastudy.be.entity.Enum.NotificationType;
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
public class RealTimeNotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private LocalDateTime createdAt;
    private Long referenceId; // ID liên quan (nếu có)
    
    public static RealTimeNotificationResponse fromEntity(Notification notification) {
        return RealTimeNotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .referenceId(notification.getReferenceId())
                .build();
    }
}