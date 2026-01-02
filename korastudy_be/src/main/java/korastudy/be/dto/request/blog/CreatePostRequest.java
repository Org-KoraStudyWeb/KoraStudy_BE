package korastudy.be.dto.request.blog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// Tạo bài viết
@Data
public class CreatePostRequest {
    private String postTitle;
    private String postSummary;
    private String postContent;
    
    @JsonProperty(value = "postPublished", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean postPublished;
    
    @JsonProperty(value = "published")
    public void setPublished(Boolean published) {
        this.postPublished = published;
    }
    
    private String featuredImage;
    private List<Long> categoryIds;
    private List<PostMetaRequest> postMetas; // Meta đi kèm
}

