package korastudy.be.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsTopicResponse {
    private Long id;
    private String title;
    private String description;
    private String icon;
    private Integer articleCount;
}
