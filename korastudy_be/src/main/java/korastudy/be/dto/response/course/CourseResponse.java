package korastudy.be.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {

    private Long id;
    private String courseName;
    private String description;
    private String courseLevel;
    private String courseImageUrl;
    private boolean published;

    private Long topicId;
    private String topicName;

    private Long certificateId;
    private String certificateName;
}
