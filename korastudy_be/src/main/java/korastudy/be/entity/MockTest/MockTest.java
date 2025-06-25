package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "mock_test")
public class MockTest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_title")
    private String title;

    @Column(name = "test_description")
    private String description;

    @Column(name = "test_level")
    private String level;

    @Column(name = "total_question")
    private Integer totalQuestions;

    @Column(name = "total_part")
    private Integer totalParts;

    @Column(name = "duration_times")
    private Integer durationTimes;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestPart> parts;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestComment> comments;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL)
    private List<ComprehensiveTestResult> testResults;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL)
    private List<PracticeTestResult> practiceTestResults;

}
