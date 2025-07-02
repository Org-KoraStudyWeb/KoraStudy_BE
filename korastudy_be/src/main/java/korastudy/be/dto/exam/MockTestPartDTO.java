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
public class MockTestPartDTO {
    private Long id;
    private Integer partNumber;
    private String title;
    private String description;
    private List<MockTestQuestionDTO> questions;
}
