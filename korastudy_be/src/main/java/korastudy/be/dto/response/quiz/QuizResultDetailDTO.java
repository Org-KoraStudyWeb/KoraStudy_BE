package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class QuizResultDetailDTO {
    private TestResultDTO summary;
    private List<AnswerResultDTO> answerDetails;
    private QuizDTO quizInfo;
}