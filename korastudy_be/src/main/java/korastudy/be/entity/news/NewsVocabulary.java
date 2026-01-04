package korastudy.be.entity.news;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

@Entity
@Table(name = "news_vocabularies", indexes = {
    @Index(name = "idx_korean_word", columnList = "korean_word")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsVocabulary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(nullable = false, length = 100)
    private String koreanWord;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String vietnameseMeaning;

    @Column(length = 200)
    private String romanization;

    @Column(length = 50)
    private String wordType; // noun, verb, adjective

    @Column(length = 20)
    private String topikLevel; // TOPIK1, TOPIK2, TOPIK3-6

    private Integer positionStart;
    private Integer positionEnd;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String contextSentence;
}
