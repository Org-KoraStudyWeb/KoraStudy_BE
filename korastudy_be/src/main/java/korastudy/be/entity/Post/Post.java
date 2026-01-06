package korastudy.be.entity.Post;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Enum.PostStatus;
import korastudy.be.entity.User.Account;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_title", columnDefinition = "NVARCHAR(255)")
    private String postTitle;

    @Column(name = "post_summary", columnDefinition = "NVARCHAR(500)")
    private String postSummary;

    @Column(name = "post_published")
    private Boolean published;

    @Lob
    @Column(columnDefinition = "nvarchar(max)")
    private String postContent;

    @Column(name = "post_published_at")
    private LocalDateTime publishedAt;

    @Column(name = "featured_image", columnDefinition = "NVARCHAR(500)")
    private String featuredImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", length = 20)
    @Builder.Default
    private PostStatus postStatus = PostStatus.PENDING;

    @ManyToMany
    @JoinTable(name = "post_category", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "categoryId"))
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostMeta> metas = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account createdBy;

    public void addMeta(PostMeta meta) {
        metas.add(meta);
        meta.setPost(this);
    }

    public void removeMeta(PostMeta meta) {
        metas.remove(meta);
        meta.setPost(null);
    }

    public void addComment(PostComment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
    }

    public void addCategory(Category category) {
        categories.add(category);
        category.getPosts().add(this);
    }

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
