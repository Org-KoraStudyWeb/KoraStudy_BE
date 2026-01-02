package korastudy.be.dto.request.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizCreateRequest {
    @NotBlank(message = "Tiêu đề quiz không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0")
    private Integer timeLimit; // phút

    @NotNull(message = "Điểm đạt không được để trống")
    @Min(value = 1, message = "Điểm đạt phải từ 1-100")
    @Max(value = 100, message = "Điểm đạt phải từ 1-100")
    private Integer passingScore; // %

    @Builder.Default
    private Boolean isPublished = false;

    @Builder.Default
    private Boolean isActive = true;

    @NotNull
    private Long sectionId;

    private List<QuestionCreateRequest> questions;
}
