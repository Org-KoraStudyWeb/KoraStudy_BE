package korastudy.be.dto.response.Exam;

import lombok.Data;

@Data
public class ExamListItemResponse {
    private Long id;
    private String title;
    private String description;
    private String level;
    private Integer totalQuestions;
    private Integer totalPart;
    private Integer durationTimes;
}
