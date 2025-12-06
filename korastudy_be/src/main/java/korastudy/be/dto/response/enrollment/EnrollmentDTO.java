package korastudy.be.dto.response.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentDTO {
    private Long id;
    private Long userId;
    private Long courseId;

    // Course info
    private String courseName;
    private String courseDescription;
    private String courseThumbnail;
    private String level;

    // Enrollment info
    private LocalDate enrollDate;
    private LocalDate expiryDate;
    private LocalDateTime lastAccessed;
    private Double progress;
    private String status;
    private Integer completedLessons;
    private Integer totalLessons;
    private Boolean isExpired;
}