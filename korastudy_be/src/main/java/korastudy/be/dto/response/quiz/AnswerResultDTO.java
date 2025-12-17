package korastudy.be.dto.response.quiz;

import korastudy.be.entity.Enum.QuestionType;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class AnswerResultDTO {
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private Double questionScore;
    private Double earnedScore;
    private Boolean isCorrect;
    private String correctAnswer;
    private String userAnswer;
    private String explanation;
    private List<OptionDTO> options;

    private Boolean userBooleanAnswer;    // true/false nếu là TRUE_FALSE
    private Boolean correctBooleanAnswer; // true/false đáp án đúng
}