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
    private QuestionType questionType;

    @Column(name = "score")
    private Double score;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "image_url", columnDefinition = "NVARCHAR(500)")
    private String imageUrl; // URL hình minh họa cho câu hỏi

    // Giải thích đáp án (hiển thị sau khi làm xong)
    @Column(name = "explanation", columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options;
}