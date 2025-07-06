package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "mock_tests")
public class MockTest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "level", columnDefinition = "NVARCHAR(50)")
    private String level;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "total_parts")
    private Integer totalParts;

    @Column(name = "duration_times")
    private Integer durationTimes;

    @Column(name = "instructions", columnDefinition = "NVARCHAR(2000)")
    private String instructions;

    @Column(name = "requirements", columnDefinition = "NVARCHAR(2000)")
    private String requirements;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestPart> parts;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestComment> comments;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL)
    private List<ComprehensiveTestResult> testResults;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL)
    private List<PracticeTestResult> practiceTestResults;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
