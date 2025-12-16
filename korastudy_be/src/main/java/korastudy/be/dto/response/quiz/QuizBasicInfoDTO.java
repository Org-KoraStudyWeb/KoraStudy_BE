// 1. Tạo QuizBasicInfoDTO (chỉ thông tin cơ bản, không có questions)
package korastudy.be.dto.response.quiz;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuizBasicInfoDTO {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer passingScore;
    private Boolean isPublished;
    private Boolean isActive;
    private Integer totalPoints;
    private Long sectionId;
    private String sectionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer questionCount;
}