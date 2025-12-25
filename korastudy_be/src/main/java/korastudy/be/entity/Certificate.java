package korastudy.be.entity;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "certificate")
public class Certificate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_code", unique = true)
    private String certificateCode;

    @Column(name = "certificate_name", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String certificateName;

    @Column(name = "certificate_date", nullable = false)
    private LocalDate certificateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Thêm các trường mới
    @Column(name = "grade")
    private String grade; // Loại certificate: EXCELLENT, GOOD, FAIR, PASS

    @Column(name = "average_score")
    private Double averageScore; // Điểm trung bình
}