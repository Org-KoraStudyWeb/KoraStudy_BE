package korastudy.be.dto.response.enrollment;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentEnrollmentDTO {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userAvatar;
    private Long courseId;
    private String courseName;
    private LocalDateTime enrollmentDate;
    private String userEmail;
}
