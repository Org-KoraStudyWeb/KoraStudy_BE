package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Enum.QuestionType;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE

    @Column(name = "score")
    private Double score;

    // Thứ tự câu hỏi trong quiz
    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options;
}