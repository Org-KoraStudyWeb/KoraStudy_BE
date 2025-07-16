package korastudy.be.dto.response.Exam;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminExamDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String level;
    private Integer totalQuestions;
    private Integer totalParts;
    private Integer durationTimes;
    private String instructions;
    private String requirements;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AdminExamPartResponse> parts;
}
