package korastudy.be.dto.request.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import korastudy.be.entity.Enum.QuestionType;
import lombok.Data;

import java.util.List;

@Data
public class QuestionUpdateRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String questionText;

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType questionType;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.5", message = "Điểm số phải từ 0.5 trở lên")
    private Double score;

    @Valid
    @Size(min = 2, message = "Câu hỏi phải có ít nhất 2 lựa chọn")
    private List<OptionUpdateRequest> options;
}
