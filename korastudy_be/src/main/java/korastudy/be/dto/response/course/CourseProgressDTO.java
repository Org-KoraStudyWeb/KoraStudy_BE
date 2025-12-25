package korastudy.be.dto.response.course;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseProgressDTO {
    private Long courseId;
    private String courseName;
    private int totalLessons;
    private int completedLessons;
    private int totalQuizzes;
    private int passedQuizzes;
    private double progressPercentage;
    private boolean isCompleted;
    private boolean hasCertificate;

}

