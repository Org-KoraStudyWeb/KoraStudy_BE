package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import korastudy.be.entity.Enum.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonCreateRequest {
    @NotBlank(message = "Tiêu đề bài học không được để trống")
    private String lessonTitle;

    private String content;

    private String videoUrl;
    private String documentUrl;

    @NotNull(message = "Loại nội dung không được để trống")
    private LessonType contentType;

    @NotNull(message = "Thứ tự không được để trống")
    private Integer orderIndex;

    private Integer duration; //số giây (seconds)

    @NotNull(message = "ID chương học không được để trống")
    private Long sectionId;
}
