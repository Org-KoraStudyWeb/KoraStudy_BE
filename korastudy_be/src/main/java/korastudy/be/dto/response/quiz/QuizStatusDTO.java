package korastudy.be.dto.response.quiz;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuizStatusDTO {
    private Long quizId;
    private String quizTitle;
    private Boolean isCompleted;      // Đã làm chưa
    private Integer attemptCount;     // Số lần đã làm
    private Double bestScore;         // Điểm cao nhất
    private Boolean isPassed;         // Đã đậu chưa
    private LocalDateTime lastAttemptDate;
    private Boolean isAvailable;      // Có được làm không (published & active)

    private Integer timeLimit;     // Thời gian làm bài
    private Integer passingScore;  // Điểm đạt
    private Boolean canRetake;     // Có được làm lại không
}
