package korastudy.be.dto.response.Exam;

import lombok.Data;

@Data
public class ExamResultResponse {
    private Long examId;
    private Integer totalQuestions;
    private Integer noCorrect;
    private Integer noIncorrect;
    private Double scores;
    private String testDate;
}
