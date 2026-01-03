package korastudy.be.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleResponse {
    private Long id;
    private String title;
    private String titleVietnamese;
    private String content;
    private String contentVietnamese;
    private String htmlContent;
    private String summary;

    private String source;
    private String sourceUrl;
    private String difficultyLevel;

    private String thumbnailUrl;
    private String audioUrl;

    private Integer readCount;
    private Boolean isBookmarked; // For current user

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    private NewsTopicResponse newsTopic;
    private List<VocabularyHighlight> vocabularies;
}
