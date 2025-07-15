package korastudy.be.dto.request.Exam;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@Data
public class UpdateExamQuestionRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String questionText;
    
    private String questionType;
    private String option;
    
    @NotBlank(message = "Đáp án đúng không được để trống")
    private String correctAnswer;
    
    private String explanation;
    
    @Min(value = 1, message = "Điểm số phải lớn hơn 0")
    private Integer points;
}
