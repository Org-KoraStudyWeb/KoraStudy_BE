package korastudy.be.dto.response;

import korastudy.be.dto.exam.TestResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatsResponse {
    private Long totalExams;
    private Long totalAttempts;
    private Double averageScore;
    private String mostPopularLevel;
    private Map<String, Long> levelDistribution;
    private Map<String, Double> levelAverageScores;
    private List<TestResultDTO> recentResults;
    private Map<String, Object> performanceMetrics;
}
