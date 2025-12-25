// korastudy.be.dto.response.quiz.UserQuizDetailedAverageScoreDTO.java
package korastudy.be.dto.response.quiz;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizDetailedAverageScoreDTO {
    private Long userId;
    private Long courseId;

    // Điểm trung bình tổng của tất cả quiz
    private Double overallAverageScore;

    // Danh sách điểm trung bình từng quiz
    private List<QuizAverageScoreDTO> quizAverages;

    // Thống kê
    private Integer totalQuizzes;
    private Integer attemptedQuizzes;
    private Integer notAttemptedQuizzes;

    private LocalDateTime lastUpdated;
    private String message;

    // ============ STATIC NESTED CLASS ============
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizAverageScoreDTO {
        private Long quizId;
        private String quizTitle;
        private Integer attemptCount;
        private Double averageScore;    // Trung bình của tất cả lần thi quiz này
        private Double bestScore;
        private LocalDateTime firstAttemptDate;
        private LocalDateTime lastAttemptDate;
    }
}