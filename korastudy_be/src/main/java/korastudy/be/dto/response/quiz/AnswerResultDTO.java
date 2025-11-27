package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AnswerResultDTO {
    private Long questionId;
    private String questionText;
    private Double questionScore;
    private Double earnedScore;
    private Boolean isCorrect;
    private String correctAnswer;
    private String userAnswer;
    private String explanation; // giải thích đáp án (nếu có)
}