package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Certificate;
import korastudy.be.entity.Grammar.CourseGrammar;
import korastudy.be.entity.Grammar.Grammar;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.Topic.Topic;
import korastudy.be.entity.Topic.TopicGroup;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "course")
public class Course extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(name = "course_level")
    private String level;

    @Column(name = "course_description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "course_image_url")
    private String imageUrl;

    @Column(name = "course_price")
    private Double price;

    @Column(name = "is_published")
    private Boolean isPublished;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<PaymentHistory> payments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<MyCourse> myCourses;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseTest> courseTests;

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL)
    private Certificate certificate;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TopicGroup> topicGroups;

}
