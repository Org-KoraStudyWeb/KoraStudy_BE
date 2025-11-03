package korastudy.be.entity.Course;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "section")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sectionName;

    private Integer orderIndex; // Thứ tự chương

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;
}
