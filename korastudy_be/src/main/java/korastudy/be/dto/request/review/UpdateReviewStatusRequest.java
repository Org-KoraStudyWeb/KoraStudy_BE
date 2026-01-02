package korastudy.be.dto.request.review;

import korastudy.be.entity.Enum.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewStatusRequest {
    private ReviewStatus status;
    private String adminNote; // Lý do admin ẩn/xóa
    private String targetType; // "COURSE" hoặc "MOCK_TEST"
}