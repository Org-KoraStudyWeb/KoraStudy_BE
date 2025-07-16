package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "mock_test_parts")
public class MockTestPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "instructions", columnDefinition = "NVARCHAR(2000)")
    private String instructions;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private MockTest mockTest;

    @OneToMany(mappedBy = "questionPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestQuestion> questions;

    @OneToMany(mappedBy = "answerPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestAnswers> answers;
}
