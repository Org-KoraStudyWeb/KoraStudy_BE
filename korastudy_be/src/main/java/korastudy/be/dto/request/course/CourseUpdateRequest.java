package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseUpdateRequest {
    @NotBlank(message = "Tên khóa học không được để trống")
    private String courseName;
    
    @NotBlank(message = "Mô tả khóa học không được để trống")
    private String courseDescription;
    
    private String courseImageUrl;
    
    private String courseLevel;
    
    private Double coursePrice;
    
    private boolean isFree;
    
    private boolean isPublished;
}
