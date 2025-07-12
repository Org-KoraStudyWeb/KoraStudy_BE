package korastudy.be.dto.request.topic;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicGroupRequestDTO {
    private String groupName;
    private String description;
}
