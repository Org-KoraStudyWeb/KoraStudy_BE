package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.Certificate;
import korastudy.be.entity.PaymentHistory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "course_name", columnDefinition = "NVARCHAR(255)")
    private String courseName;

    @Column(name = "course_description", columnDefinition = "NVARCHAR(MAX)")
    private String courseDescription;

    @Column(name = "course_image_url")
    private String courseImageUrl;

    @Column(name = "is_published")
    private boolean isPublished;

    @Column(name = "course_level", columnDefinition = "NVARCHAR(100)")
    private String courseLevel;

    @Column(name = "course_price")
    private Double coursePrice;

    @Column(name = "is_free")
    private boolean isFree;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    // Quan hệ: 1 khóa học có nhiều Section
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections;

    // Quan hệ: 1 khóa học có nhiều Review
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    // Quan hệ: nhiều user đăng ký nhiều course (Enrollment)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<PaymentHistory> paymentHistories = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Certificate> certificates = new ArrayList<>();
}