package korastudy.be.dto.response.quiz;

import korastudy.be.entity.Enum.QuestionType;
import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class QuestionDTO {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Double score;
    private List<OptionDTO> options;
    private Integer orderIndex;
}