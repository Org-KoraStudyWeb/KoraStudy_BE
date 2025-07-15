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

    // ✅ Dành cho FE: tên gộp sẵn
    private String authorName;

    // ✅ Nếu cần chi tiết account
    private AccountResponse createdBy;

    private List<PostMetaResponse> postMetas;

    public static PostResponse fromEntity(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setPostTitle(post.getPostTitle());
        response.setPostSummary(post.getPostSummary());
        response.setPostContent(post.getPostContent());
        response.setPostPublished(post.getPublished());

        // ✅ Lấy tác giả
        if (post.getCreatedBy() != null) {
            AccountResponse author = AccountResponse.fromEntity(post.getCreatedBy());
            response.setCreatedBy(author);
            response.setAuthorName(formatUserName(author));
        }

        // ✅ Lấy metas nếu có
        if (post.getMetas() != null && !post.getMetas().isEmpty()) {
            response.setPostMetas(
                    post.getMetas().stream()
                            .map(PostMetaResponse::fromEntity)
                            .toList()
            );
        }

        return response;
    }

    // ✅ Helper format: First Last (fallback username)
    private static String formatUserName(AccountResponse user) {
        if (user == null) return null;

        String fn = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String ln = user.getLastName() != null ? user.getLastName().trim() : "";

        String fullName = (fn + " " + ln).trim();

        return !fullName.isEmpty() ? fullName : user.getUsername();
    }
}
