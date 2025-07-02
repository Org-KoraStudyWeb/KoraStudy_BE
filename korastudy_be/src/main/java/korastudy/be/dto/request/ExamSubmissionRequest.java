package korastudy.be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionRequest {
    
    @NotNull(message = "Mock test ID is required")
    private Long mockTestId;
    
    @NotNull(message = "Test type is required")
    private String testType;
    
    @Valid
    @NotNull(message = "Answers are required")
    private List<AnswerSubmissionRequest> answers;
    
    private Integer durationMinutes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerSubmissionRequest {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        @NotNull(message = "Selected answer is required")
        private String selectedAnswer;
    }
}
