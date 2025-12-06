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

    @Column(name = "lesson_title", columnDefinition = "NVARCHAR(500)")
    private String lessonTitle;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "document_url")
    private String documentUrl; //THÊM: cho tài liệu PDF/Word

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private LessonType contentType; //SỬA: VIDEO, TEXT, QUIZ, DOCUMENT

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "duration")
    private Integer duration; // THÊM: thời lượng (phút)

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonProgress> progresses;
}