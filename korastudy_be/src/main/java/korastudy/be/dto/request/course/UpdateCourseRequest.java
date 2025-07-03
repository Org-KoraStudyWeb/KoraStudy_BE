package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCourseRequest {

    private String courseName;

    private String description;

    private String courseLevel;

    private String courseImageUrl;

    private Long topicId;

    private Long certificateId;

    private Boolean published;
}
