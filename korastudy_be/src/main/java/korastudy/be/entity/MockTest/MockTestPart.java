package korastudy.be.entity.MockTest;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "test_part")
public class MockTestPart extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer partNumber;

    private String title;

    @Column(name = "part_description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private MockTest mockTest;

    @OneToMany(mappedBy = "questionPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestQuestion> questions;

    @OneToMany(mappedBy = "answerPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestAnswers> answers;
}
