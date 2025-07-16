package korastudy.be.dto.response.Exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultResponse {
    private Long examId;
    private Long resultId;
    private Integer totalQuestions;
    private Integer noCorrect;
    private Integer noIncorrect;
    private Double scores;
    private Integer earnedPoints;
    private Integer totalPoints;
    private String testDate;
    private List<ExamAnswerDetailResponse> answerDetails; // Đảm bảo field này có
}
