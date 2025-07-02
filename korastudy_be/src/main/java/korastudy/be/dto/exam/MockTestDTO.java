package korastudy.be.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTestDTO {
    private Long id;
    private String title;
    private String description;
    private String level;
    private Integer totalQuestions;
    private Integer totalParts;
    private Integer durationTimes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MockTestPartDTO> parts;
    private Long completionCount;
    private Double averageScore;
}
