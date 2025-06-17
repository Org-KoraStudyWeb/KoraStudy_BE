package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "comprehensive_test_result")
public class MockTestResult extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_result_id")
    private Long id;

    @Column(name = "test_result_type")
    private String testType;

    @Column(name = "test_date")
    private LocalDateTime testDate;

    @Column(name = "result_correct")
    private Integer noCorrect;

    @Column(name = "result_incorrect")
    private Integer noIncorrect;

    @Column(name = "scores")
    private Double scores;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private MockTest mockTest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
