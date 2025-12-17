package korastudy.be.dto.request.quiz;

import jakarta.validation.constraints.NotNull;
import korastudy.be.entity.Enum.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    @NotNull(message = "ID câu hỏi không được để trống")
    private Long questionId;

    // Cho SINGLE_CHOICE, TRUE_FALSE
    private Long selectedOptionId;

    // Cho MULTIPLE_CHOICE
    private List<Long> selectedOptionIds;

    // Cho ESSAY
    private String essayAnswer;

    // Cho FILL_IN_BLANK
    private String fillInBlankAnswer;

}