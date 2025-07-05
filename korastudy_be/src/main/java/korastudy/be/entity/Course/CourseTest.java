package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "course_test")
public class CourseTest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseTestId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "duration_times")
    private Integer durationTimes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "courseTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseTestQuestion> questions;

    @OneToMany(mappedBy = "courseTest", cascade = CascadeType.ALL)
    private List<CourseTestResult> results;

    @OneToMany(mappedBy = "courseTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionTest> sectionTests;

}
