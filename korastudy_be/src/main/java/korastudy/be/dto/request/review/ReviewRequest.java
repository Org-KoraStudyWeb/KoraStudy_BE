package korastudy.be.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import korastudy.be.entity.Enum.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
    @NotNull(message = "Loại đánh giá không được để trống")
    private ReviewType reviewType; // COURSE hoặc MOCK_TEST

    @NotNull(message = "ID không được để trống")
    private Long targetId; // courseId, mockTestId, hoặc newsArticleId tùy theo reviewType

    @Min(value = 0, message = "Đánh giá phải từ 0 đến 5 sao")
    @Max(value = 5, message = "Đánh giá phải từ 0 đến 5 sao")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String comment;
}
