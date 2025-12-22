package korastudy.be.dto.response.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizProgressSummaryDTO {
    private Long userId;
    private String username;
    private String displayName;
    private Long courseId;
    private String courseTitle;

    // Thống kê
    private Integer totalQuizzes;      // Tổng số quiz trong course
    private Integer publishedQuizzes;  // Số quiz đã published
    private Integer attemptedQuizzes;  // Số quiz đã làm
    private Integer passedQuizzes;     // Số quiz đã pass
    private Integer notStartedQuizzes; // Số quiz chưa làm
    private Integer failedQuizzes;     // Số quiz đã làm nhưng fail

    // Điểm số
    private Double averageScore;       // Điểm trung bình
    private Double passRate;           // Tỷ lệ pass (%)
    private Double completionRate;     // Tỷ lệ hoàn thành (%)

    // Thời gian
    private Long totalTimeSpent;       // Tổng thời gian làm bài (giây)
    private LocalDateTime firstAttemptDate; // Lần làm đầu tiên
    private LocalDateTime lastAttemptDate;  // Lần làm gần nhất
}
