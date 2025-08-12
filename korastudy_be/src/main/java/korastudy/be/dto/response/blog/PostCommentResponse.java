package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.PostComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostCommentResponse {
    private Long id;
    private String context;
    private String username;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private Long parentId;
    private List<PostCommentResponse> children;

    public static PostCommentResponse fromEntity(PostComment comment) {
        String fullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        return PostCommentResponse.builder()
                .id(comment.getId())
                .context(comment.getContext())
                .username(fullName)
                .isPublished(comment.getIsPublished())
                .publishedAt(comment.getPublishedAt())
        .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
        .children(comment.getChildren() == null ? List.of() :
            comment.getChildren().stream()
                .map(PostCommentResponse::fromEntity)
                .collect(Collectors.toList()))
                .build();
    }
}
