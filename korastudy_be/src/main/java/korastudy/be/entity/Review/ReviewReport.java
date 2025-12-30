package korastudy.be.entity.Review;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reason", columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "resolved")
    private boolean resolved = false;

    // THÊM 2 trường mới
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "admin_note", columnDefinition = "NVARCHAR(MAX)")
    private String adminNote;

    // THÊM các phương thức helper
    public void markAsResolved(String note) {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.adminNote = note;
    }

    public void markAsUnresolved() {
        this.resolved = false;
        this.resolvedAt = null;
        this.adminNote = null;
    }

    // Kiểm tra xem report đã được xử lý chưa
    public boolean isPending() {
        return !resolved;
    }

    // Thời gian từ khi report đến khi resolve (nếu đã resolve)
    public Long getResolutionTimeInHours() {
        if (resolved && resolvedAt != null && getCreatedAt() != null) {
            return java.time.Duration.between(getCreatedAt(), resolvedAt).toHours();
        }
        return null;
    }
}