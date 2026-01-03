package korastudy.be.entity.news;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "news_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsTopic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String description;

    private String icon; // Emoji or icon class

    @OneToMany(mappedBy = "newsTopic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsArticle> articles = new ArrayList<>();
}
