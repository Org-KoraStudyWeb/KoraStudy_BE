package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Vocabulary.GrammarSection;
import korastudy.be.entity.Vocabulary.VocabularySection;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "course_section")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sectionTitle;

    private String sectionContent;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VocabularySection> vocabularies;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrammarSection> grammars;

    @OneToOne(mappedBy = "section", cascade = CascadeType.ALL)
    private SectionTest test;
}
