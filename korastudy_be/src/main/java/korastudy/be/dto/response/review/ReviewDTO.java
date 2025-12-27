package korastudy.be.dto.response.review;

import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;

    // THÊM: Loại review
    private ReviewType reviewType;

    // THAY ĐỔI: Không chỉ courseId nữa
    private Long targetId; // ID của course hoặc mockTest

    // THÊM: Tên của course/mockTest để hiển thị
    private String targetTitle;

    // THÊM: Loại target để FE biết là course hay mockTest
    private String targetType; // "COURSE" hoặc "MOCK_TEST"

    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private ReviewStatus status;
    private String adminNote;
}