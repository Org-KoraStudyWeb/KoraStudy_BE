package korastudy.be.dto.response.course;

import korastudy.be.dto.response.topic.TopicGroupResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    private List<TopicGroupResponseDTO> topicGroups;
}
