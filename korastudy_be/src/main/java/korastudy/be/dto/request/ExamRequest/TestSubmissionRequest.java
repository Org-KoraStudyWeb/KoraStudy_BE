package korastudy.be.dto.request.ExamRequest;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestSubmissionRequest {
    private Long testId;
    private Long userId;
    private String testType; // "PRACTICE" or "COMPREHENSIVE"
    private List<AnswerSubmission> answers;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AnswerSubmission {
        private Long questionId;
        private String selectedAnswer;
        private Long partId;
    }
}

