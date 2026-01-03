package korastudy.be.entity.news;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User.User;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reading_history", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "article_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReadingHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal readingProgress = BigDecimal.ZERO; // 0-100%

    @Column(nullable = false)
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer wordsLookedUp = 0;

    private LocalDateTime lastReadAt;

    public void updateProgress(BigDecimal progress, Integer timeSpent, Boolean isCompleted) {
        this.readingProgress = progress;
        this.timeSpentSeconds = timeSpent;
        this.completed = isCompleted;
        this.lastReadAt = LocalDateTime.now();
    }

    public void incrementWordsLookedUp() {
        this.wordsLookedUp++;
    }
}
