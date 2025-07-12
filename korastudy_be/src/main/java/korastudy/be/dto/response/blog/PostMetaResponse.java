package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.PostMeta;
import lombok.Data;

@Data
public class PostMetaResponse {
    private Long id;
    private String metaKey;
    private String postMetaContext;

    public static PostMetaResponse fromEntity(PostMeta meta) {
        PostMetaResponse res = new PostMetaResponse();
        res.setId(meta.getId());
        res.setMetaKey(meta.getMetaKey());
        res.setPostMetaContext(meta.getPostMetaContext());
        return res;
    }
}
