package korastudy.be.entity.Topic;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Grammar.Grammar;
import korastudy.be.entity.Vocabulary.Vocabulary;
import korastudy.be.entity.Vocabulary.VocabularyProgress;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "topic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String topicName;

    @Column(name = "topic_description", columnDefinition = "NVARCHAR(255)")
    private String topicDescription;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Vocabulary> vocabularies;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<VocabularyProgress> vocabularyProgresses;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Grammar> grammars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_group_id", nullable = false)
    private TopicGroup topicGroup;

    @OneToOne(mappedBy = "topic", cascade = CascadeType.ALL)
    private TopicTest topicTest;

}
