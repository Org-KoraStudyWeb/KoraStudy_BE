package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizSummaryDTO {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer passingScore;
    private Integer questionCount;
    private Long lessonId;
    private String lessonTitle;
    private LocalDateTime createdAt;
}
