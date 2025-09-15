package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionCreateRequest {
    @NotBlank(message = "Tên chương không được để trống")
    private String sectionName;
    
    @NotNull(message = "Thứ tự hiển thị không được để trống")
    private Integer orderIndex;
    
    @NotNull(message = "ID khóa học không được để trống")
    private Long courseId;
}
