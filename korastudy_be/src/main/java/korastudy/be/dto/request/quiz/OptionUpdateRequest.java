package korastudy.be.dto.request.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionUpdateRequest {
    @NotBlank(message = "Nội dung lựa chọn không được để trống")
    private String optionText;

    @NotNull(message = "Trạng thái đúng/sai không được để trống")
    private Boolean isCorrect;

    private String imageUrl;
}