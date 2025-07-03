package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCourseRequest {

    @NotBlank(message = "Tên khóa học không được để trống")
    private String courseName;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    private String courseLevel;

    private String courseImageUrl;

    @NotNull(message = "ID chủ đề là bắt buộc")
    private Long topicId;

    @NotNull(message = "ID chứng chỉ là bắt buộc")
    private Long certificateId;

    private boolean published = false;
}
