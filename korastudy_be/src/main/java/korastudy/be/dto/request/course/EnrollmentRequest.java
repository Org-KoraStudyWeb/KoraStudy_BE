package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentRequest {
    @NotNull(message = "ID khóa học không được để trống")
    private Long courseId;
    
    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;
}
