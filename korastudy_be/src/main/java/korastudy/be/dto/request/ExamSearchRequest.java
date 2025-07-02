package korastudy.be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSearchRequest {
    private String title;
    private String level;
    private String difficulty;
    private Integer minQuestions;
    private Integer maxQuestions;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
