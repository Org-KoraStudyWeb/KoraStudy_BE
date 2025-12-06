package korastudy.be.dto.response.quiz;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class QuizAnswerDTO {
    private Long id;
    private Long questionId;
    private String questionText;
    private String userAnswer;
    private Boolean isCorrect;
    private Double earnedScore;
    private Double questionScore; // Điểm tối đa của câu hỏi
    private String correctAnswer; // Đáp án đúng (cho review)
    private String explanation;   // Giải thích (nếu có)
}
