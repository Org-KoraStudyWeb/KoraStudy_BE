package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TestResultDTO {
    private Long id;
    private Double score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer passingScore;
    private Boolean isPassed;
    private LocalDateTime takenDate;
    private Long timeSpent; // giây
    private Long quizId;
    private String quizTitle;
    private Long userId;
    private String username;

    // Danh sách câu trả lời chi tiết
    private List<QuizAnswerDTO> answerDetails;
}