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
    private String name;        // Frontend expects 'name'
    private String title;       // Keep for backward compatibility
    private String description;
    private String icon;
    private Integer articleCount;
}
