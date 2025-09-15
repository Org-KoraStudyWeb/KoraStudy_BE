package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.Enum.LessonType;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "lesson")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lessonTitle;

    @Column(columnDefinition = "TEXT")
    private String content; // nội dung chi tiết (Markdown/HTML)

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private LessonType contentType; // VIDEO, TEXT

    private Integer orderIndex; // Thứ tự bài trong chương

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    // Tiến độ học viên
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonProgress> progresses;
}
