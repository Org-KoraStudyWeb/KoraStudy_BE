package korastudy.be.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {
    private Long id;
    private String testType;
    private LocalDateTime testDate;
    private Integer noCorrect;
    private Integer noIncorrect;
    private Integer totalQuestions;
    private Double score;
    private Integer durationMinutes;
    private String level;
    private String testTitle;
    private Long mockTestId;
    private String username;
}
