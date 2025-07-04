package korastudy.be.dto.request.Exam;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Long questionId;
    private String selectedAnswer;
}
