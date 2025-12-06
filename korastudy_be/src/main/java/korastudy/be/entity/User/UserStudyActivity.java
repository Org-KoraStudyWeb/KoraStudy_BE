package korastudy.be.entity.User;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user_study_activities")
public class UserStudyActivity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "study_duration_minutes")
    private Integer studyDurationMinutes = 0;

    @Column(name = "activities_count")
    private Integer activitiesCount = 0;

    @Column(name = "last_activity_time")
    private LocalDateTime lastActivityTime;

    @PrePersist
    protected void onCreate() {
        if (lastActivityTime == null) {
            lastActivityTime = LocalDateTime.now();
        }
    }
}
