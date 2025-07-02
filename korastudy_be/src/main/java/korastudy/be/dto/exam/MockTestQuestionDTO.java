package korastudy.be.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTestQuestionDTO {
    private Long id;
    private String option;
    private String imageUrl;
    private String audioUrl;
    private String questionText;
    private List<MockTestAnswerDTO> answers;
}
