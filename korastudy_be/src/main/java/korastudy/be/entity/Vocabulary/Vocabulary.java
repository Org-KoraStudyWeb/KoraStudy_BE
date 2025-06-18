package korastudy.be.entity.Vocabulary;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "vocabulary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary extends BaseTimeEntity {

    @Id
    @Column(name = "vocabulary_id")
    private String id;

    @Column(nullable = false)
    private String word;

    private String meaning;

    private String level;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "audio_url")
    private String audioUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL)
    private List<VocabularyProgress> progress;
}
