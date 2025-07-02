package korastudy.be.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTestAnswerDTO {
    private Long id;
    private String selectedAnswer;
    private Boolean isCorrect;
}
