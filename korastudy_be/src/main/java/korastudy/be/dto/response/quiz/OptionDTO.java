package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OptionDTO {
    private Long id;
    private String optionText;
    private Boolean isCorrect;
    private Integer orderIndex;
}