package korastudy.be.dto.request.course;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTopicRequest {

    private String topicName;

    private String topicDescription;

    private String topicImageUrl;

    private Long courseId;
}
