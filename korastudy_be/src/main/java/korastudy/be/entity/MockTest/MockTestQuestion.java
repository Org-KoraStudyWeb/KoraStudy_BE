package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "mock_test_questions")
public class MockTestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(2000)")
    private String questionText;

    @Column(name = "question_type", columnDefinition = "NVARCHAR(50)")
    private String questionType;

    @Column(name = "options", columnDefinition = "NVARCHAR(2000)")
    private String option;

    @Column(name = "correct_answer", columnDefinition = "NVARCHAR(500)")
    private String correctAnswer;

    @Column(name = "explanation", columnDefinition = "NVARCHAR(1000)")
    private String explanation;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(name = "points")
    private Integer points = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private MockTestPart questionPart;

    @OneToMany(mappedBy = "questionAnswer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestAnswers> answers;
}
