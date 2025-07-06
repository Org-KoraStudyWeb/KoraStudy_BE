package korastudy.be.dto.request.Exam;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@Data
public class UpdateExamPartRequest {
    @NotBlank(message = "Tiêu đề phần thi không được để trống")
    private String title;
    
    private String description;
    private String instructions;
    
    @Min(value = 1, message = "Thời gian phần thi phải lớn hơn 0")
    private Integer timeLimit;
}
