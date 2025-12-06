package korastudy.be.dto.response.course;

import korastudy.be.entity.Enum.ProgressStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonProgressDTO {
    private Long id;
    private ProgressStatus status;
    private Long timeSpent; // giây
    private Double progress; // 0.0 - 1.0
    private LocalDateTime startedDate;
    private LocalDateTime completedDate;
    private LocalDateTime lastAccessed;
    private Long lessonId;
    private String lessonTitle;
    private Long userId;
    private String username;
}