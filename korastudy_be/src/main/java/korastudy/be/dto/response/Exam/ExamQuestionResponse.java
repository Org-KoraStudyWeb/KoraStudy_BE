package korastudy.be.dto.response.Exam;

import lombok.Data;

@Data
public class ExamQuestionResponse {
    private Long questionId;
    private String questionText;
    private String option;
    private String imageUrl;
    private String audioUrl;
}