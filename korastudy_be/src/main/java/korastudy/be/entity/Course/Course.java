package korastudy.be.entity.Course;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    @Column(columnDefinition = "TEXT")
    private String courseDescription;

    private String courseImageUrl;

    private boolean isPublished;

    private String courseLevel;

    private Double coursePrice;

    private boolean isFree;

    private Long viewCount = 0L;

    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    // Quan hệ: 1 khóa học có nhiều Section
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections;

    // Quan hệ: 1 khóa học có nhiều Quiz
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes;

    // Quan hệ: 1 khóa học có nhiều Review
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    // Quan hệ: nhiều user đăng ký nhiều course (Enrollment)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;
}
