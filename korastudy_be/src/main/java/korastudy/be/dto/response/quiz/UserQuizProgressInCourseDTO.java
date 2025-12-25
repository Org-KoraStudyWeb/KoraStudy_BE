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
public class UserQuizProgressInCourseDTO {
    private Long quizId;
    private String quizTitle;
    private Long sectionId;
    private String sectionTitle;
    private Integer orderIndex; // Thứ tự trong section

    // Thông tin quiz
    private Double passingScore;
    private Integer timeLimit; // Thời gian làm bài (phút)
    private Boolean isPublished;

    // Trạng thái của user
    private Boolean isCompleted;    // Đã làm quiz chưa
    private Boolean isPassed;       // Đã pass chưa
    private Double bestScore;       // Điểm cao nhất
    private Integer attemptCount;   // Số lần làm
    private LocalDateTime lastAttemptDate; // Lần làm gần nhất

    // Thông tin chi tiết (nếu có)
    private Double latestScore;     // Điểm lần làm gần nhất
    private Long timeSpent;         // Thời gian làm (giây)
    private LocalDateTime takenDate; // Ngày làm gần nhất
}