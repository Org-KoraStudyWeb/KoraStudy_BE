package korastudy.be.service.impl;

import korastudy.be.dto.request.review.ReviewRequest;
import korastudy.be.dto.request.review.UpdateReviewStatusRequest;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.entity.MockTest.MockTest;
import korastudy.be.entity.Review;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.MockTestRepository;
import korastudy.be.repository.ReviewRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IEnrollmentService;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final MockTestRepository mockTestRepository;
    private final IEnrollmentService enrollmentService;

    @Override
    @Transactional
    public ReviewDTO addReview(Long userId, ReviewRequest request) {
        log.info("Adding review for user: {}, type: {}, targetId: {}",
                userId, request.getReviewType(), request.getTargetId());

        User user = getUserOrThrow(userId);

        return switch (request.getReviewType()) {
            case COURSE -> addCourseReview(user, request);
            case MOCK_TEST -> addMockTestReview(user, request);
        };
    }

    private ReviewDTO addCourseReview(User user, ReviewRequest request) {
        Long courseId = request.getTargetId();

        validateCourseEnrollment(user.getId(), courseId);
        checkExistingReview(user.getId(), courseId, ReviewType.COURSE);

        Course course = getCourseOrThrow(courseId);
        Review review = createReview(user, request, course, null);
        Review savedReview = reviewRepository.save(review);

        log.info("Added review for course: {}, by user: {}", courseId, user.getId());
        return mapToDTO(savedReview);
    }

    private ReviewDTO addMockTestReview(User user, ReviewRequest request) {
        Long mockTestId = request.getTargetId();

        checkExistingReview(user.getId(), mockTestId, ReviewType.MOCK_TEST);

        MockTest mockTest = getMockTestOrThrow(mockTestId);
        Review review = createReview(user, request, null, mockTest);
        Review savedReview = reviewRepository.save(review);

        log.info("Added review for mock test: {}, by user: {}", mockTestId, user.getId());
        return mapToDTO(savedReview);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = getReviewOrThrow(reviewId);

        validateReviewOwnership(review, userId);
        validateReviewIsActive(review);

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Updated review: {} by user: {}", reviewId, userId);
        return mapToDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        validateReviewOwnership(review, userId);
        reviewRepository.delete(review);
        log.info("Deleted review: {} by user: {}", reviewId, userId);
    }

    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        return mapToDTO(review);
    }

    @Override
    public List<ReviewDTO> getCourseReviews(Long courseId) {
        List<Review> reviews = reviewRepository.findByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageCourseRating(Long courseId) {
        Double avgRating = reviewRepository.findAverageRatingByCourseId(courseId);
        return formatRating(avgRating);
    }

    @Override
    public List<ReviewDTO> getMockTestReviews(Long mockTestId) {
        List<Review> reviews = reviewRepository.findByMockTestIdAndStatus(mockTestId, ReviewStatus.ACTIVE);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageMockTestRating(Long mockTestId) {
        Double avgRating = reviewRepository.findAverageRatingByMockTestId(mockTestId);
        return formatRating(avgRating);
    }

    @Override
    public Page<ReviewDTO> getCourseReviewsWithPagination(Long courseId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE, pageable);
        return reviewPage.map(this::mapToDTO);
    }

    @Override
    public Page<ReviewDTO> getMockTestReviewsWithPagination(Long mockTestId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByMockTestIdAndStatus(mockTestId, ReviewStatus.ACTIVE, pageable);
        return reviewPage.map(this::mapToDTO);
    }

    @Override
    public long countReviewsByCourseId(Long courseId) {
        return reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE);
    }

    @Override
    public long countReviewsByMockTestId(Long mockTestId) {
        return reviewRepository.countByMockTestIdAndStatus(mockTestId, ReviewStatus.ACTIVE);
    }

    @Override
    public boolean hasUserReviewedCourse(Long userId, Long courseId) {
        return reviewRepository.findByUserIdAndCourseId(userId, courseId).isPresent();
    }

    @Override
    public boolean hasUserReviewedMockTest(Long userId, Long mockTestId) {
        return reviewRepository.findByUserIdAndMockTestId(userId, mockTestId).isPresent();
    }

    @Override
    public List<ReviewDTO> getUserReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdAndStatus(userId, ReviewStatus.ACTIVE);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewDTO> getUserReviewsWithPagination(Long userId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByUserIdAndStatus(userId, ReviewStatus.ACTIVE, pageable);
        return reviewPage.map(this::mapToDTO);
    }

    @Override
    public Page<ReviewDTO> getAllReviews(String targetType, ReviewStatus status, Pageable pageable) {
        ReviewType reviewType = targetType != null ? ReviewType.valueOf(targetType.toUpperCase()) : null;
        Page<Review> reviewPage = reviewRepository.findReviewsWithFilters(reviewType, status, null, pageable);
        return reviewPage.map(this::mapToDTO);
    }

    @Override
    @Transactional
    public ReviewDTO updateReviewStatus(Long reviewId, UpdateReviewStatusRequest request) {
        Review review = getReviewOrThrow(reviewId);
        review.setStatus(request.getStatus());
        Review updatedReview = reviewRepository.save(review);
        log.info("Updated review status: {} to {}", reviewId, request.getStatus());
        return mapToDTO(updatedReview);
    }

    @Override
    public ReviewDTO mapToDTO(Review review) {
        ReviewDTO.ReviewDTOBuilder builder = ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getDisplayName())
                .userAvatar(review.getUser().getAvatar())
                .reviewType(review.getReviewType())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .lastModified(review.getLastModified())
                .status(review.getStatus())
                .adminNote(null);

        if (review.getReviewType() == ReviewType.COURSE && review.getCourse() != null) {
            builder.targetId(review.getCourse().getId())
                    .targetTitle(review.getCourse().getCourseName())
                    .targetType("COURSE");
        } else if (review.getReviewType() == ReviewType.MOCK_TEST && review.getMockTest() != null) {
            builder.targetId(review.getMockTest().getId())
                    .targetTitle(review.getMockTest().getTitle())
                    .targetType("MOCK_TEST");
        }

        return builder.build();
    }

    // ========== HELPER METHODS ==========

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));
    }

    private MockTest getMockTestOrThrow(Long mockTestId) {
        return mockTestRepository.findById(mockTestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + mockTestId));
    }

    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
    }

    private void validateCourseEnrollment(Long userId, Long courseId) {
        if (!enrollmentService.isUserEnrolledInCourse(userId, courseId)) {
            throw new IllegalStateException("Bạn cần đăng ký khóa học trước khi đánh giá");
        }
    }

    private void checkExistingReview(Long userId, Long targetId, ReviewType reviewType) {
        reviewRepository.findByUserIdAndTargetId(userId, targetId)
                .ifPresent(r -> {
                    String message = reviewType == ReviewType.COURSE
                            ? "Bạn đã đánh giá khóa học này rồi"
                            : "Bạn đã đánh giá bài thi này rồi";
                    throw new IllegalStateException(message);
                });
    }

    private Review createReview(User user, ReviewRequest request, Course course, MockTest mockTest) {
        return Review.builder()
                .user(user)
                .course(course)
                .mockTest(mockTest)
                .reviewType(request.getReviewType())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.ACTIVE)
                .build();
    }

    private void validateReviewOwnership(Review review, Long userId) {
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    private void validateReviewIsActive(Review review) {
        if (review.getStatus() != ReviewStatus.ACTIVE) {
            throw new IllegalStateException("Không thể chỉnh sửa đánh giá đã bị ẩn/xóa");
        }
    }

    private double formatRating(Double rating) {
        return rating != null ? Math.round(rating * 10.0) / 10.0 : 0.0;
    }
}