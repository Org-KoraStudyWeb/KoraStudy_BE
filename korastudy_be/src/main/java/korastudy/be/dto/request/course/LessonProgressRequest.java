package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonProgressRequest {
    @NotNull(message = "ID bài học không được để trống")
    private Long lessonId;

    @NotNull(message = "Trạng thái không được để trống")
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    private Long timeSpent; // seconds
    private Double progress; // 0.0 - 1.0
}