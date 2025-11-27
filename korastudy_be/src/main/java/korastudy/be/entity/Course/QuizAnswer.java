package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.User.User;
import lombok.*;

@Entity
@Table(name = "quiz_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_result_id")
    private TestResult testResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    // Lưu câu trả lời của user (tuỳ loại câu hỏi)
    @Column(name = "user_answer", columnDefinition = "NVARCHAR(MAX)")
    private String userAnswer; // JSON hoặc text tuỳ loại câu hỏi

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "earned_score")
    private Double earnedScore; // Điểm đạt được cho câu này

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
}