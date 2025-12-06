package korastudy.be.entity.Course;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "document_url")
    private String documentUrl; // URL từ Cloudinary

    @Column(name = "file_type")
    private String fileType; // PDF, DOC, PPT, etc.

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
}