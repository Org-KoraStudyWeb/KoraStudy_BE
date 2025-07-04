package korastudy.be.dto.response.Exam;

import lombok.Data;

import java.util.List;

@Data
public class ExamPartResponse {
    private Long partId;
    private Integer partNumber;
    private String title;
    private String description;
    private List<ExamQuestionResponse> questions;
}
