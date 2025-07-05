package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "section_test")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionTest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private int durationMinutes;

    @OneToOne
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @OneToMany(mappedBy = "sectionTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionTestQuestion> questions;

    @ManyToOne
    @JoinColumn(name = "course_test_id", nullable = false)
    private CourseTest courseTest;

}
