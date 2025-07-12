package korastudy.be.dto.request.blog;

import lombok.Data;

import java.util.List;

// Tạo bài viết
@Data
public class CreatePostRequest {
    private String postTitle;
    private String postSummary;
    private String postContent;
    private Boolean postPublished;
    private List<PostMetaRequest> postMetas; // Meta đi kèm
}