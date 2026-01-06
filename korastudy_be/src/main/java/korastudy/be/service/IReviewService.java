package korastudy.be.service;

import korastudy.be.dto.request.review.ReviewRequest;
import korastudy.be.dto.request.review.UpdateReviewStatusRequest;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.dto.response.review.ReviewStatsDTO;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IReviewService {

    // 1. Tạo review mới (cho cả Course và MockTest)
    ReviewDTO addReview(Long userId, ReviewRequest request);

    // 2. Cập nhật review (chỉ chủ sở hữu)
    ReviewDTO updateReview(Long userId, Long reviewId, ReviewRequest request);

    // 3. Xóa review (chỉ chủ sở hữu)
    void deleteReview(Long userId, Long reviewId);


    //Xóa review chỉ dành cho ADMIN
    void deleteReviewAsAdmin(Long adminId, Long reviewId);


    // 4. Lấy review theo ID
    ReviewDTO getReviewById(Long reviewId);
    //  Lấy review theo ID cho admin
    ReviewDTO getReviewDetailForAdmin(Long reviewId, Long adminId);

    // 5. Lấy tất cả review của một khóa học (chỉ Course)
    List<ReviewDTO> getCourseReviews(Long courseId);

    // 6. Tính điểm trung bình rating của khóa học (chỉ Course)
    double getAverageCourseRating(Long courseId);

    // 7. Lấy tất cả review của một bài thi (MockTest)
    List<ReviewDTO> getMockTestReviews(Long mockTestId);

    // 8. Tính điểm trung bình rating của bài thi
    double getAverageMockTestRating(Long mockTestId);

    // News Reviews (Comments)
    List<ReviewDTO> getNewsReviews(Long newsArticleId);


    // 9. Phân trang reviews của khóa học
    Page<ReviewDTO> getCourseReviewsWithPagination(Long courseId, Pageable pageable);

    // 10. Phân trang reviews của bài thi
    Page<ReviewDTO> getMockTestReviewsWithPagination(Long mockTestId, Pageable pageable);

    // Phân trang reviews của News
    Page<ReviewDTO> getNewsReviewsWithPagination(Long newsArticleId, Pageable pageable);

    // 11. Đếm số review của khóa học
    long countReviewsByCourseId(Long courseId);

    // 12. Đếm số review của bài thi
    long countReviewsByMockTestId(Long mockTestId);

    // Đếm số review của News
    long countReviewsByNewsArticleId(Long newsArticleId);

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

                            //==============================Stats của review==========//

    ReviewStatsDTO getReviewStatsDTO();

    ReviewStatsDTO getCourseReviewStatsDTO(Long courseId);

    ReviewStatsDTO getMockTestReviewStatsDTO(Long mockTestId);

    // Giữ các phương thức cũ (Map) cho backward compatibility
    Map<String, Object> getReviewStats();

    Map<String, Object> getCourseReviewStats(Long courseId);

    Map<String, Object> getMockTestReviewStats(Long mockTestId);

    // Thêm phương thức cho dashboard charts
    Map<String, Object> getDashboardStats();

    // Report a review
    @Transactional
    ReviewDTO reportReview(Long reviewId, String reason, Long userId);

    // Like a review
    @Transactional
    ReviewDTO likeReview(Long reviewId, Long userId);

    // Unlike a review
    @Transactional
    ReviewDTO unlikeReview(Long reviewId, Long userId);

    // Check if user has liked a review
    boolean hasUserLikedReview(Long reviewId, Long userId);

    // Get like count for a review
    Long getLikesCount(Long reviewId);

    // Get reported reviews for admin
    Page<ReviewDTO> getReportedReviews(Pageable pageable);

    // Resolve a reported review (admin action)
    @Transactional
    ReviewDTO resolveReport(Long reviewId, boolean takeAction, String adminNote);
}