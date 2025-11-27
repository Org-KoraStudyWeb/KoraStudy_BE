package korastudy.be.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private Long courseId;
    private String courseName;
    private LocalDate enrollDate;
    private LocalDate expiryDate;
    private double progress;
}
