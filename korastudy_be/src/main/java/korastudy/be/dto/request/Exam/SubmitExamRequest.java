package korastudy.be.dto.request.Exam;

import lombok.Data;
import lombok.ToString;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ToString
public class SubmitExamRequest {
    
    @NotEmpty(message = "Danh sách câu trả lời không được rỗng")
    @Valid
    private List<SubmitAnswerRequest> answers;
}