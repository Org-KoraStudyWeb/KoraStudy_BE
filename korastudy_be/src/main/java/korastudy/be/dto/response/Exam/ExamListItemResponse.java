package korastudy.be.dto.response.Exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamListItemResponse {
    private Long id;
    private String title;
    private String description;
    private String level;
    private Integer totalQuestions;
    private Integer totalPart;
    private Integer durationTimes;
    private Boolean isActive;
    private Long totalTaken; // Số người đã làm bài
    private Double averageRating; // Điểm đánh giá trung bình (1-5)
    private Long reviewCount; // Số lượt đánh giá
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
