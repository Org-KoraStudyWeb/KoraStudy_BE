package korastudy.be.dto.response.course;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CourseProgressDetailDTO {
    private Long courseId;
    private String courseName;

    // Lessons
    private int totalLessons;
    private int completedLessons;
    private double lessonCompletionRate;

    // Quizzes
    private int totalQuizzes;
    private int passedQuizzes;
    private double quizPassRate;
    private double averageQuizScore;

    // Overall
    private boolean isCompleted;
    private boolean hasCertificate;  // THÊM TRƯỜNG NÀY
    private String motivationalMessage;
    private List<String> recommendations;

    // Certificate info (nếu đã có)
    private String certificateGrade;
    private String certificateMessage;
}