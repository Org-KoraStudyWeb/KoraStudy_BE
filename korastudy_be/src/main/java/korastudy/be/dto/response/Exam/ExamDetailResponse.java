package korastudy.be.dto.response.Exam;

import lombok.Data;

import java.util.List;

@Data
public class ExamDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String level;
    private Integer totalQuestions;
    private Integer totalPart;
    private Integer durationTimes;
    private List<ExamPartResponse> parts;
    private List<String> instructions;
    private List<String> requirements;
}