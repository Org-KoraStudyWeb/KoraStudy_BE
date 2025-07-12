package korastudy.be.dto.response.blog;

import korastudy.be.dto.response.account.AccountResponse;
import korastudy.be.entity.Post.Post;
import lombok.Data;

import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private String postTitle;
    private String postSummary;
    private String postContent;
    private Boolean postPublished;
    private String authorName; // ✅ Gán sẵn tên
    private AccountResponse createdBy; // ✅ Thêm author gốc
    private List<PostMetaResponse> postMetas;

    public static PostResponse fromEntity(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setPostTitle(post.getPostTitle());
        response.setPostSummary(post.getPostSummary());
        response.setPostContent(post.getPostContent());
        response.setPostPublished(post.getPublished());

        if (post.getCreatedBy() != null) {
            AccountResponse author = AccountResponse.fromEntity(post.getCreatedBy());
            response.setCreatedBy(author);
            response.setAuthorName(formatUserName(author));
        }

        if (post.getMetas() != null) {
            response.setPostMetas(post.getMetas().stream()
                    .map(PostMetaResponse::fromEntity)
                    .toList());
        }

        return response;
    }

    private static String formatUserName(AccountResponse user) {
        if (user == null) return null;
        String fn = user.getFirstName() != null ? user.getFirstName() : "";
        String ln = user.getLastName() != null ? user.getLastName() : "";
        String full = (fn + " " + ln).trim();
        return !full.isEmpty() ? full : user.getUsername();
    }
}
