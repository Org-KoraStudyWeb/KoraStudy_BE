package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.PostComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostCommentResponse {
    private Long id;
    private String context;
    private String username;
    private Boolean isPublished;
    private LocalDateTime publishedAt;

    public static PostCommentResponse fromEntity(PostComment comment) {
        String fullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        return PostCommentResponse.builder()
                .id(comment.getId())
                .context(comment.getContext())
                .username(fullName)
                .isPublished(comment.getIsPublished())
                .publishedAt(comment.getPublishedAt())
                .build();
    }
}
