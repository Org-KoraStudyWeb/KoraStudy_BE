package korastudy.be.entity.FlashCard;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.news.NewsFlashcardMapping;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String definition;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String example; // Câu ví dụ minh họa

    private String imageUrl; // Link ảnh minh họa (nếu cần)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private SetCard setCard;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCardProgress> progresses = new ArrayList<>();

    // Cascade delete for news flashcard mappings
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NewsFlashcardMapping> newsFlashcardMappings = new ArrayList<>();

}

