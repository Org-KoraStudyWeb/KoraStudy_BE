package korastudy.be.entity.Course;

import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Enum.EnrollmentStatus;
import korastudy.be.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== RELATIONSHIPS ====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // ==================== DATES ====================
    @Column(nullable = false)
    private LocalDate enrollDate;

    private LocalDate expiryDate;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    // ==================== PROGRESS & STATUS ====================
    @Builder.Default
    private Double progress = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    // ==================== LEARNING STATS ====================
    @Builder.Default
    private Integer completedLessons = 0;

    // ==================== AUDIT FIELDS ====================
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ==================== METHODS ====================
    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return this.status == EnrollmentStatus.COMPLETED;
    }

    public boolean isExpired() {
        return this.expiryDate != null && LocalDate.now().isAfter(this.expiryDate);
    }

    public void markAsCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
        this.progress = 100.0;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}