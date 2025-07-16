package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "practice_test_results")
public class PracticeTestResult extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_type", columnDefinition = "NVARCHAR(50)")
    private String testType = "PRACTICE";

    @Column(name = "test_date")
    private LocalDateTime testDate;

    @Column(name = "no_correct")
    private Integer noCorrect;

    @Column(name = "no_incorrect")
    private Integer noIncorrect;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "scores")
    private Double scores;

    @Column(name = "earned_points")
    private Integer earnedPoints;

    @Column(name = "total_points")
    private Integer totalPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_test_id")
    private MockTest mockTest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "practice_test_parts", joinColumns = @JoinColumn(name = "result_id"))
    @Column(name = "part_id")
    private List<Long> completedParts;

    @PrePersist
    protected void onCreate() {
        testDate = LocalDateTime.now();
    }
}
