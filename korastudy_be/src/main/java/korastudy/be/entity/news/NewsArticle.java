package korastudy.be.entity.news;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Enum.ArticleStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "news_articles", indexes = {
    @Index(name = "idx_news_topic", columnList = "news_topic_id"),
    @Index(name = "idx_difficulty_level", columnList = "difficulty_level"),
    @Index(name = "idx_published_at", columnList = "published_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_topic_id")
    private NewsTopic newsTopic;

    // Content
    @Column(nullable = false, length = 500, columnDefinition = "NVARCHAR(500)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String titleVietnamese;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String contentVietnamese;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String htmlContent; // Formatted HTML with highlights

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String summary;

    // Metadata
    @Column(length = 100)
    private String source; // VOA Korea, KBS, Arirang

    @Column(length = 1000)
    private String sourceUrl;

    @Column(length = 20)
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT; // DRAFT, SCHEDULED, PUBLISHED

    // Media
    @Column(length = 1000)
    private String thumbnailUrl;

    @Column(length = 1000)
    private String audioUrl;

    // Stats
    @Column(nullable = false)
    @Builder.Default
    private Integer readCount = 0;

    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsVocabulary> vocabularies = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserReadingHistory> readingHistories = new ArrayList<>();

    public void incrementReadCount() {
        this.readCount++;
    }
}
