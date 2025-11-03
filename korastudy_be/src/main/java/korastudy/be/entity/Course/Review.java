package korastudy.be.entity.Course;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import korastudy.be.entity.User.User;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating; // 1-5 sao

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

