package korastudy.be.dto.response.Exam;

import lombok.Data;
import java.util.List;

@Data
public class AdminExamPartResponse {
    private Long id;
    private Integer partNumber;
    private String title;
    private String description;
    private String instructions;
    private Integer questionCount;
    private Integer timeLimit;
    private List<AdminExamQuestionResponse> questions;
}
