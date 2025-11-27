package korastudy.be.dto.response.quiz;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizStatisticsDTO {
    private Long quizId;
    private String quizTitle;

    // Admin xem thống kê toàn bộ học viên
    private Integer totalParticipants;      // Tổng số học viên làm
    private Integer passedParticipants;     // Số học viên đậu
    private Double averageScore;            // Điểm trung bình
    private Double passingRate;              // Tỷ lệ đậu
    private String lastSubmissionDate;

}
