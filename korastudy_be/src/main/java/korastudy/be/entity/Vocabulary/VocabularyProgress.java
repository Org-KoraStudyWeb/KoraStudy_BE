package korastudy.be.entity.Vocabulary;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User;
import lombok.*;

@Entity
@Table(name = "vocabulary_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyProgress extends BaseTimeEntity {

    @EmbeddedId
    private VocabularyProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("user")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vocabulary")
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("topic")
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "is_learned")
    private Boolean isLearned;

    @Column(name = "is_proficiented")
    private Boolean isProficiented;
}