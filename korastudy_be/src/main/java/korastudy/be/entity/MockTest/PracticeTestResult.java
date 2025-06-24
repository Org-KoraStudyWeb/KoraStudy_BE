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
@Table(name = "practice_test_result")
public class PracticeTestResult extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_test_id")
    private Long id;


    @Column(name = "test_type")
    private String testType;

    @Column(name = "test_date")
    private LocalDateTime testDate;

    @Column(name = "no_correct")
    private Integer noCorrect;

    @Column(name = "no_incorrect")
    private Integer noIncorrect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private MockTest mockTest;
}
