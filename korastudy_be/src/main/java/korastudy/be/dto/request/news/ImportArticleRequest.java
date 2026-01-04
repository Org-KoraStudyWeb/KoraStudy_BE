package korastudy.be.dto.request.news;

import lombok.Data;

@Data
public class ImportArticleRequest {
    private String url;
    private Long topicId; // Optional - gán topic ngay khi import
}
