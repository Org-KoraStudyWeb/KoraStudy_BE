package korastudy.be.service;

import korastudy.be.dto.request.review.ReviewRequest;
import korastudy.be.dto.request.review.UpdateReviewStatusRequest;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IReviewService {

    // 1. Tạo review mới (cho cả Course và MockTest)
    ReviewDTO addReview(Long userId, ReviewRequest request);

    // 2. Cập nhật review (chỉ chủ sở hữu)
    ReviewDTO updateReview(Long userId, Long reviewId, ReviewRequest request);

    // 3. Xóa review (chỉ chủ sở hữu)
    void deleteReview(Long userId, Long reviewId);

    // 4. Lấy review theo ID
    ReviewDTO getReviewById(Long reviewId);

    // 5. Lấy tất cả review của một khóa học (chỉ Course)
    List<ReviewDTO> getCourseReviews(Long courseId);

    // 6. Tính điểm trung bình rating của khóa học (chỉ Course)
    double getAverageCourseRating(Long courseId);

    // 7. Lấy tất cả review của một bài thi (MockTest)
    List<ReviewDTO> getMockTestReviews(Long mockTestId);

    // 8. Tính điểm trung bình rating của bài thi
    double getAverageMockTestRating(Long mockTestId);


    // 9. Phân trang reviews của khóa học
    Page<ReviewDTO> getCourseReviewsWithPagination(Long courseId, Pageable pageable);

    // 10. Phân trang reviews của bài thi
    Page<ReviewDTO> getMockTestReviewsWithPagination(Long mockTestId, Pageable pageable);

    // 11. Đếm số review của khóa học
    long countReviewsByCourseId(Long courseId);

    // 12. Đếm số review của bài thi
    long countReviewsByMockTestId(Long mockTestId);

    // 13. Kiểm tra user đã review khóa học chưa
    boolean hasUserReviewedCourse(Long userId, Long courseId);

    // 14. Kiểm tra user đã review bài thi chưa
    boolean hasUserReviewedMockTest(Long userId, Long mockTestId);

    // 15. Lấy tất cả review của một user
    List<ReviewDTO> getUserReviews(Long userId);

    Page<ReviewDTO> getUserReviewsWithPagination(Long userId, Pageable pageable);

    // 16. Lấy tất cả review với filter (cho admin)
    Page<ReviewDTO> getAllReviews(String targetType, ReviewStatus status, Pageable pageable);

    // 17. Cập nhật trạng thái review (admin)
    ReviewDTO updateReviewStatus(Long reviewId, UpdateReviewStatusRequest request);

    // 18. Convert Entity to DTO
    ReviewDTO mapToDTO(Review review);
}