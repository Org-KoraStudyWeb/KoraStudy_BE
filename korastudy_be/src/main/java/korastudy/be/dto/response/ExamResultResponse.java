package korastudy.be.dto.response;

import korastudy.be.dto.exam.TestResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultResponse {
    private Long resultId;
    private Long mockTestId;
    private String testTitle;
    private String testType;
    private LocalDateTime testDate;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Double score;
    private Double percentage;
    private Integer durationMinutes;
    private String level;
    private String grade;
    private Map<String, Object> breakdown;
    private List<QuestionResultDetail> questionDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultDetail {
        private Long questionId;
        private String questionText;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private String explanation;
    }
}
