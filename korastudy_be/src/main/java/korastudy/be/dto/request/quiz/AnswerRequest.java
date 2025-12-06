package korastudy.be.dto.request.quiz;

import jakarta.validation.constraints.NotNull;
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

    private List<Long> selectedOptionIds; // MULTIPLE_CHOICE
    private Long selectedOptionId;        // SINGLE_CHOICE
    private String essayAnswer;           // ESSAY
    private Boolean trueFalseAnswer;      // TRUE_FALSE
}