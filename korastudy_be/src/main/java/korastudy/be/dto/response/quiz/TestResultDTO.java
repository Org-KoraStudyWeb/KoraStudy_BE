package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TestResultDTO {
    private Long id;
    private Double score;           // Điểm phần trăm
    private Double earnedPoints;    //  Điểm số thực tế
    private Double totalPoints;     //  Tổng điểm tối đa

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

    private List<AnswerResultDTO> answerDetails;

    // So sánh với lần làm trước
    private Double previousScore;
    private Boolean isImproved;
}