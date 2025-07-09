package korastudy.be.dto.request.Exam;

import lombok.Data;
import lombok.ToString;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@ToString
public class SubmitAnswerRequest {
    
    @NotNull(message = "ID câu hỏi không được null")
    private Long questionId;
    
    @NotBlank(message = "Câu trả lời được chọn không được để trống")
    private String selectedAnswer;
}
