package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User;
import lombok.*;

@Entity
@Table(name = "course_test_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseTestResult extends BaseTimeEntity {

    @Id
    @Column(name = "course_test_result_id")
    private String id;

    private Double scores;

    @Column(name = "no_correct")
    private Integer noCorrect;

    @Column(name = "no_incorrect")
    private Integer noIncorrect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_test_id", nullable = false)
    private CourseTest courseTest;
}
