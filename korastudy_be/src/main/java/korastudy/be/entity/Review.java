package korastudy.be.entity;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.entity.MockTest.MockTest;
import lombok.*;

import korastudy.be.entity.User.User;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating")
    private int rating; // 1-5 sao

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "review_type")
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType; // COURSE hoặc MOCK_TEST

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // Có thể NULL nếu review cho MockTest

    @ManyToOne
    @JoinColumn(name = "mock_test_id")
    private MockTest mockTest; // Có thể NULL nếu review cho Course

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.ACTIVE;
}