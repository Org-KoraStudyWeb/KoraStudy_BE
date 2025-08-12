package korastudy.be.entity;

import jakarta.persistence.*;
import korastudy.be.entity.User.User;
import korastudy.be.entity.Enum.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;
    
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;
    
    private boolean read;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType type = NotificationType.SYSTEM; // Mặc định là thông báo hệ thống
    
    // Optional: ID liên quan (post_id, comment_id, etc.)
    @Column(name = "reference_id")
    private Long referenceId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Mặc định: thông báo tồn tại 1 giờ
        expirationDate = createdAt.plusHours(1);
    }
}