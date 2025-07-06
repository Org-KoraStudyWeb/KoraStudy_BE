package korastudy.be.entity.Grammar;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Topic.Topic;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "grammar")
public class Grammar extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grammar_name")
    private String name;

    private String meaning;
    private String level;
    private String imageUrl;
    private String audioUrl;

    @OneToMany(mappedBy = "grammar", cascade = CascadeType.ALL)
    private List<CourseGrammar> courseGrammars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
}

