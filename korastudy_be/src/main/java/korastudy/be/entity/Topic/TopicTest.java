package korastudy.be.entity.Topic;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "topic_test")
public class TopicTest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalQuestions;
    private Integer durationMinutes;

    @OneToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @OneToMany(mappedBy = "topicTest", cascade = CascadeType.ALL)
    private List<TopicTestQuestion> questions;

    @OneToMany(mappedBy = "topicTest", cascade = CascadeType.ALL)
    private List<TopicTestResult> results;
}
