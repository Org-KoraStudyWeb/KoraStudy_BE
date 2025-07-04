package korastudy.be.dto.request.Exam;

import lombok.Data;

import java.util.List;

@Data
public class SubmitExamRequest {
    private List<SubmitAnswerRequest> answers;
}