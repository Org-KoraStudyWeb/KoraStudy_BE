package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

@Entity
@Table(name = "option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_text", columnDefinition = "NVARCHAR(1000)")
    private String optionText;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "image_url", columnDefinition = "NVARCHAR(500)")
    private String imageUrl; // Hình ảnh của đáp án

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}