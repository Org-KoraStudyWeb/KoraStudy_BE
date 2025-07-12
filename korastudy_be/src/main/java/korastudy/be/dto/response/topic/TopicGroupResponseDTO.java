package korastudy.be.dto.response.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicGroupResponseDTO {
    private Long id;
    private String groupName;
    private String description;
}
