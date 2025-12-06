package korastudy.be.dto.request.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuizSubmissionRequest {

    private Long timeSpent;
    @Valid
    @NotNull(message = "Câu trả lời không được để trống")
    private List<AnswerRequest> answers;
}