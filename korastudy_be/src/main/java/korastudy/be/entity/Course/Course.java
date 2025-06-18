package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Certificate;
import korastudy.be.entity.Grammar;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.Vocabulary.Topic;
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
    @Column(name = "course_id", nullable = false, unique = true)
    private String id;

    @Column(name = "course_name", nullable = false)
    private String name;

    @Column(name = "course_level")
    private String level;

    @Column(name = "course_description", columnDefinition = "TEXT")
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

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Grammar> grammars;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseTest> courseTests;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Topic> topics;
}
