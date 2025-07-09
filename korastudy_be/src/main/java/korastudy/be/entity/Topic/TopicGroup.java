package korastudy.be.entity.Topic;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Course.Course;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "topic_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String groupName;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "topicGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Topic> topics;
}
