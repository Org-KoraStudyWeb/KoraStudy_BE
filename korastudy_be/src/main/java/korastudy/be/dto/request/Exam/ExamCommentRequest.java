package korastudy.be.dto.request.Exam;

import lombok.Data;

@Data
public class ExamCommentRequest {
    private String context;
    private Long userId;
}
