package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;
    private LocalDateTime takenDate;
    private Long timeSpent; // Thời gian làm bài (giây)

    private Double totalPoints;      // Tổng điểm tối đa của quiz
    private Double earnedPoints;     // Điểm thực tế đạt được
    private Integer correctAnswers;  // Số câu trả lời đúng
    private Integer totalQuestions;  // Tổng số câu hỏi
    private Boolean isPassed;        // Đã vượt qua bài test hay chưa

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // THÊM: Quan hệ với QuizAnswer
    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> quizAnswers;


    public Double getScore() {
        return score != null ? score : 0.0;  // Sửa getter để không bao giờ trả về null
    }
}