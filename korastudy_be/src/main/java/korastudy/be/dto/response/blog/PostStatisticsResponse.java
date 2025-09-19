package korastudy.be.dto.response.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatisticsResponse {
    private Long totalPosts;
    private Long publishedPosts;
    private Long draftPosts;
    private Long totalComments;
    private Long totalCategories;
    private Long totalViews;
    private List<CategoryStatsDTO> topCategories;
    private List<PostViewDTO> mostViewedPosts;
    private Map<String, Long> postsByMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatsDTO {
        private Long id;
        private String name;
        private Long postCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostViewDTO {
        private Long id;
        private String title;
        private Integer viewCount;
    }
}