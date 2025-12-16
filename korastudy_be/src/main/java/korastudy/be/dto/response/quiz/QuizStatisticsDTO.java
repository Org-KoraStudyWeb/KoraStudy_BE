package korastudy.be.dto.response.quiz;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuizStatisticsDTO {
    private Long quizId;
    private String quizTitle;
    private Integer totalAttempts;
    private Integer passedAttempts;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Double averageTimeSpent;
    private LocalDateTime firstAttemptDate;
    private LocalDateTime lastAttemptDate;

    @Data
    @Builder
    public static class QuestionStat {
        private Long questionId;
        private String questionText;
        private Double correctRate; // Tỉ lệ trả lời đúng
        private Integer totalAttempts;
        private Integer correctAttempts;
    }

    private List<QuestionStat> questionStats;
}