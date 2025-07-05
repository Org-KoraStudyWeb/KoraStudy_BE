package korastudy.be.entity.Course;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "section_test_question")
public class SectionTestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionText;  // Nội dung câu hỏi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_test_id", nullable = false)
    private SectionTest sectionTest;  // Liên kết đến phần kiểm tra

    @Column(nullable = false)
    private String correctAnswer;  // Đáp án đúng

    @Column(nullable = true)
    private String imageUrl;  // Nếu có hình ảnh

    @Column(nullable = true)
    private String audioUrl;  // Nếu có âm thanh
}

