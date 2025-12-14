package korastudy.be.dto.request.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class QuestionUpdateRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String questionText;

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType questionType;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.5", message = "Điểm số phải từ 0.5 trở lên")
    private Double score;

    @Min(value = 1, message = "Thứ tự phải lớn hơn 0")
    private Integer orderIndex;

    // Hình ảnh minh họa cho câu hỏi (optional)
    private String imageUrl;

    // Giải thích đáp án sau khi làm xong (optional)
    private String explanation;


    // Danh sách options (cần cho TẤT CẢ loại câu hỏi, kể cả FILL_IN_BLANK)
    @Valid
    private List<OptionUpdateRequest> options;

    // Kiểm tra options dựa vào questionType
    @AssertTrue(message = "Câu hỏi trắc nghiệm phải có ít nhất 2 lựa chọn")
    public boolean isOptionsValid() {
        switch (questionType) {
            case SINGLE_CHOICE:
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                return options != null && options.size() >= 2;
            case FILL_IN_BLANK:
                //  FILL_IN_BLANK cần ít nhất 1 option
                return options != null && !options.isEmpty();
            case ESSAY:
                // ESSAY không cần options
                return true;
            default:
                return true;
        }
    }

    @AssertTrue(message = "Câu hỏi TRUE_FALSE chỉ được có 2 lựa chọn")
    public boolean isTrueFalseValid() {
        if (questionType == QuestionType.TRUE_FALSE) {
            return options != null && options.size() == 2;
        }
        return true;
    }

    @AssertTrue(message = "Câu hỏi SINGLE_CHOICE phải có đúng 1 đáp án đúng")
    public boolean isSingleChoiceValid() {
        if (questionType == QuestionType.SINGLE_CHOICE) {
            if (options == null) return false;
            long correctCount = options.stream().filter(OptionUpdateRequest::getIsCorrect).count();
            return correctCount == 1;
        }
        return true;
    }

    // Validation cho FILL_IN_BLANK
    @AssertTrue(message = "Câu hỏi FILL_IN_BLANK chỉ có đáp án đúng (không có đáp án sai)")
    public boolean isFillInBlankValid() {
        if (questionType == QuestionType.FILL_IN_BLANK && options != null) {
            // Tất cả options của FILL_IN_BLANK phải là correct
            return options.stream().allMatch(OptionUpdateRequest::getIsCorrect);
        }
        return true;
    }

    // Validation cho MULTIPLE_CHOICE
    @AssertTrue(message = "Câu hỏi MULTIPLE_CHOICE phải có ít nhất 1 đáp án đúng")
    public boolean isMultipleChoiceValid() {
        if (questionType == QuestionType.MULTIPLE_CHOICE && options != null) {
            long correctCount = options.stream().filter(OptionUpdateRequest::getIsCorrect).count();
            return correctCount >= 1;
        }
        return true;
    }

    // Validation cho ESSAY
    @AssertTrue(message = "Câu hỏi ESSAY không được có options")
    public boolean isEssayValid() {
        if (questionType == QuestionType.ESSAY) {
            return options == null || options.isEmpty();
        }
        return true;
    }
}