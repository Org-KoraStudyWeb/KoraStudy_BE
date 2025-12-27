package korastudy.be.entity;

import jakarta.persistence.*;
import korastudy.be.entity.User.User;
import korastudy.be.entity.Enum.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
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
    private NotificationType type = NotificationType.SYSTEM;

    @Column(name = "reference_id")
    private Long referenceId;

    // THÊM FIELD NÀY - QUAN TRỌNG!
    @Column(name = "is_published", nullable = false)
    @Builder.Default  // Nếu dùng @Builder
    private Boolean isPublished = false;  // Giá trị mặc định

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expirationDate = createdAt.plusHours(1);

        // Đảm bảo isPublished có giá trị nếu vẫn null
        if (isPublished == null) {
            isPublished = false;
        }
    }
}