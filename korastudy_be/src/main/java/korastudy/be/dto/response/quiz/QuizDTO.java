package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer passingScore;

    private Long sectionId;
    private String sectionName;

    private List<QuestionDTO> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}