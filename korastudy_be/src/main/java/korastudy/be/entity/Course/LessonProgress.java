package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.Enum.ProgressStatus;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    private Long timeSpent; // thời gian học (giây/phút)
    private LocalDateTime completedDate;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
