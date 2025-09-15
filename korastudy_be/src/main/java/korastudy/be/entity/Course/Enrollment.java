package korastudy.be.entity.Course;

import jakarta.persistence.*;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "enrollment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate enrollDate;
    private LocalDate expiryDate;

    private Double progress; // % tiến độ chung của course

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Entity User đã có sẵn

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
