package korastudy.be.dto.response.Exam;

import lombok.Data;

@Data
public class AdminExamQuestionResponse {
    private Long id;
    private String questionText;
    private String questionType;
    private String option;
    private String correctAnswer;
    private String explanation;
    private String imageUrl;
    private String audioUrl;
    private Integer questionOrder;
    private Integer points;
}
