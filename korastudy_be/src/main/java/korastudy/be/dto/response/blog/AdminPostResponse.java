package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostResponse {
    private Long id;
    private String postTitle;
    private String postSummary;
    private String postContent;
    private Boolean published;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Long authorId;
    private String authorUsername;
    private String authorName;
    private List<CategoryResponse> categories;
    private List<PostMetaResponse> metas;
    private Integer commentCount;

    public static AdminPostResponse fromEntity(Post post) {
        return AdminPostResponse.builder()
                .id(post.getId())
                .postTitle(post.getPostTitle())
                .postSummary(post.getPostSummary())
                .postContent(post.getPostContent())
                .published(post.getPublished())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .viewCount(post.getViewCount())
                .authorId(post.getCreatedBy().getId())
                .authorUsername(post.getCreatedBy().getUsername())
                .authorName(post.getCreatedBy().getUser() != null ?
                        post.getCreatedBy().getUser().getFirstName() + " " +
                                post.getCreatedBy().getUser().getLastName() : "")
                .categories(post.getCategories().stream()
                        .map(CategoryResponse::fromEntity)
                        .collect(Collectors.toList()))
                .metas(post.getMetas().stream()
                        .map(PostMetaResponse::fromEntity)
                        .collect(Collectors.toList()))
                .commentCount(post.getComments().size())
                .build();
    }
}