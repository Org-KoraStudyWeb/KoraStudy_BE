package korastudy.be.dto.response.enrollment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentDetailDTO {
    private Long id;

    // User info (CHO ADMIN)
    private Long userId;
    private String username;
    private String avatar;
    private String email;

    // Course info
    private Long courseId;
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

    // Admin fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
