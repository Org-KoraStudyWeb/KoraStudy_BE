package korastudy.be.dto.response.quiz;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class QuizAllResultsDTO {
    private Long quizId;
    private String quizTitle;
    private List<UserResultDTO> userResults;

    @Data
    @Builder
    public static class UserResultDTO {
        private Long userId;
        private String username;
        private Double score;
        private Boolean isPassed;
        private LocalDateTime takenDate;
        private Long timeSpent;
    }
}