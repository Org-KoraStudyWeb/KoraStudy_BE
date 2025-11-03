package korastudy.be.dto.request.blog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCommentRequest {
    private String context;
    // Optional parent comment id to create a reply; null for top-level
    private Long parentId;
}
