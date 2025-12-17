package korastudy.be.dto.response.quiz;

import korastudy.be.entity.Enum.QuestionType;
import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class QuestionDTO {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Double score;
    private List<OptionDTO> options;
    private Integer orderIndex;

    private String imageUrl;           // Hình minh họa
    private String explanation;        // Giải thích đáp án
    private String correctAnswer;      // Đáp án đúng (cho FILL_IN_BLANK, ESSAY)

    // Cho teacher/admin xem đáp án
    private Boolean showCorrectAnswer; // Có hiển thị đáp án không
}