package korastudy.be.dto.request.blog;

import lombok.Data;

// Thêm/Cập nhật meta
@Data
public class PostMetaRequest {
    private String metaKey;
    private String postMetaContext;
}