package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.PostComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentResponse {
    private Long id;
    private String commentContent;
    private Long postId;
    private String postTitle;
    private Long authorId;
    private String authorUsername;
    private String authorName;
    private LocalDateTime createdAt;

    public static AdminCommentResponse fromEntity(PostComment comment) {
        return AdminCommentResponse.builder()
                .id(comment.getId())
                .commentContent(comment.getContext())  // Chú ý: cần kiểm tra field thực tế trong entity PostComment
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getPostTitle())
                .authorId(comment.getUser().getId())  // Chú ý: cần kiểm tra field thực tế
                .authorUsername(comment.getUser().getAccount().getUsername())
                .authorName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}