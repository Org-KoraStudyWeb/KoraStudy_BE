package korastudy.be.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String courseName;
    private String courseDescription;
    private String courseImageUrl;
    private String courseLevel;
    private Double coursePrice;
    private boolean isFree;
    private boolean isPublished;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private List<SectionDTO> sections;
    private double averageRating;
    private int enrollmentCount;
}
