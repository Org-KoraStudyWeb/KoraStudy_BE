package korastudy.be.dto.response.Exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerDetailResponse {
    private Long questionId;
    private String questionText;
    private String selectedAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer points;
}
