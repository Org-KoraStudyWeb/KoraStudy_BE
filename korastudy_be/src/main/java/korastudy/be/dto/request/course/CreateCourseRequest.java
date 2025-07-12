package korastudy.be.dto.request.course;

import korastudy.be.dto.request.topic.TopicGroupRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCourseRequest {

    private String name;
    private String level;
    private String description;
    private String imageUrl;
    private Double price;
    private Boolean isPublished;

    private List<TopicGroupRequestDTO> topicGroups; // thêm dòng này
}
