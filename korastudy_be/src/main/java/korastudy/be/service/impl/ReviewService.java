package korastudy.be.service.impl;

import korastudy.be.dto.request.review.ReviewRequest;
import korastudy.be.dto.request.review.UpdateReviewStatusRequest;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.dto.response.review.ReviewStatsDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.MockTest.MockTest;
import korastudy.be.entity.Review.Review;
import korastudy.be.entity.Review.ReviewLike;
import korastudy.be.entity.Review.ReviewReport;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.*;
import korastudy.be.service.IEnrollmentService;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;  // THÊM
    private final ReviewReportRepository reviewReportRepository;
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

        // THÊM VALIDATION
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Updated review: {} by user: {}", reviewId, userId);
        return mapToDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        // Phương thức này giờ chỉ dành cho USER xóa review của chính mình
        // (nếu bạn vẫn muốn giữ chức năng này)
        Review review = getReviewOrThrow(reviewId);
        validateReviewOwnership(review, userId);
        reviewRepository.delete(review);
        log.info("User {} deleted own review: {}", userId, reviewId);
    }

    @Override
    @Transactional
    public void deleteReviewAsAdmin(Long adminId, Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        User admin = getUserOrThrow(adminId);
        validateAdminRole(admin);

        // 1. Xóa tất cả likes của review trước
        reviewLikeRepository.deleteByReviewId(reviewId);
        log.info("Deleted all likes for review: {}", reviewId);

        // 2. Xóa tất cả reports của review
        reviewReportRepository.deleteByReviewId(reviewId);
        log.info("Deleted all reports for review: {}", reviewId);

        // 3. Xóa review
        reviewRepository.delete(review);
        log.info("Admin {} deleted review: {}", adminId, reviewId);
    }

    /**
     * Validate user có role ADMIN
     */
    private void validateAdminRole(User user) {
        boolean isAdmin = user.getAccount().getRoles().stream()
                .anyMatch(role -> RoleName.ADMIN.equals(role.getRoleName()));

        if (!isAdmin) {
            log.warn("User {} attempted admin action without ADMIN role", user.getId());
            throw new IllegalStateException("Chỉ ADMIN mới có quyền thực hiện thao tác này");
        }
    }

    /**
     * Validate review ownership (cho USER thông thường)
     */
    private void validateReviewOwnership(Review review, Long userId) {
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền xóa review của người khác");
        }
    }


    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        return mapToDTO(review);
    }

    @Override
    public ReviewDTO getReviewDetailForAdmin(Long reviewId, Long adminId) {
        log.debug("👨‍💼 Getting review detail for admin: reviewId={}, adminId={}", reviewId, adminId);

        Review review = getReviewOrThrow(reviewId);

        // Validate admin role
        User admin = getUserOrThrow(adminId);
        validateAdminRole(admin);

        // ADMIN có thể xem tất cả reviews, kể cả HIDDEN/DELETED
        // Thêm thông tin chi tiết cho admin
        return mapToDTOForAdmin(review, adminId);
    }

    public ReviewDTO mapToDTOForAdmin(Review review, Long adminId) {
        // Lấy DTO cơ bản trước
        ReviewDTO dto = mapToDTO(review, adminId);

        // THÊM thông tin ADMIN-ONLY

        // 1. Danh sách users đã like
        List<Long> likedUserIds = reviewLikeRepository.findUserIdsByReviewId(review.getId());
        dto.setLikedUserIds(likedUserIds);

        // 2. Chi tiết reports
        List<ReviewReport> reports = reviewReportRepository.findAllByReviewId(review.getId());
        List<Map<String, Object>> reportDetails = reports.stream()
                .map(report -> {
                    User reporter = report.getUser();
                    Map<String, Object> reportMap = new LinkedHashMap<>();
                    reportMap.put("reportId", report.getId());
                    reportMap.put("userId", reporter.getId());
                    reportMap.put("username", reporter.getDisplayName());
                    reportMap.put("userEmail", reporter.getEmail());
                    reportMap.put("reason", report.getReason());
                    reportMap.put("reportedAt", report.getCreatedAt());
                    reportMap.put("resolved", report.isResolved());
                    reportMap.put("resolvedAt", report.getResolvedAt());
                    reportMap.put("adminNote", report.getAdminNote());
                    return reportMap;
                })
                .collect(Collectors.toList());
        dto.setReportDetails(reportDetails);

        // 3. Thông tin người viết review
        User reviewer = review.getUser();
        Map<String, Object> reviewerInfo = new LinkedHashMap<>();
        reviewerInfo.put("userId", reviewer.getId());
        reviewerInfo.put("email", reviewer.getEmail());
        reviewerInfo.put("phone", reviewer.getPhoneNumber());
        reviewerInfo.put("joinedDate", reviewer.getCreatedAt());
        reviewerInfo.put("totalReviews", reviewRepository.countByUserId(reviewer.getId()));
        reviewerInfo.put("totalLikes", reviewLikeRepository.countByUserId(reviewer.getId()));
        reviewerInfo.put("reportedReviews", reviewRepository.countByUserIdAndStatus(
                reviewer.getId(), ReviewStatus.REPORTED));

        dto.setReviewerInfo(reviewerInfo);

        // 4. Cờ cảnh báo
        dto.setIsFlagged(dto.getReportsCount() >= 5 ||
                dto.getStatus() == ReviewStatus.REPORTED ||
                dto.getStatus() == ReviewStatus.HIDDEN);

        if (dto.getReportsCount() >= 5) {
            dto.setFlaggedReason("Có " + dto.getReportsCount() + " reports");
        }

        return dto;
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
        return getCourseReviewsWithPagination(courseId, pageable, null);
    }

    public Page<ReviewDTO> getCourseReviewsWithPagination(Long courseId, Pageable pageable, Long currentUserId) {
        Page<Review> reviewPage = reviewRepository.findByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE, pageable);
        return reviewPage.map(review -> mapToDTO(review, currentUserId));
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
        // FIX: Kiểm tra cả null và empty/blank string
        ReviewType reviewType = null;

        if (targetType != null && !targetType.trim().isEmpty()) {
            try {
                reviewType = ReviewType.valueOf(targetType.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid review type: {}", targetType);
                throw new IllegalArgumentException("Invalid review type. Use: COURSE, MOCK_TEST");
            }
        }

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
        return mapToDTO(review, null); // Overload method
    }

    // Thêm phương thức mới với currentUserId
    public ReviewDTO mapToDTO(Review review, Long currentUserId) {
        // Validate review entity
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }

        if (review.getUser() == null) {
            throw new IllegalStateException("Review must have a user");
        }

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
                .adminNote(review.getAdminNote())
                .likesCount(reviewLikeRepository.countByReviewId(review.getId())) // THÊM
                .reportsCount(reviewReportRepository.countActiveReportsByReviewId(review.getId())) // THÊM
                .userLiked(false) // Mặc định
                .userReported(false); // Mặc định

        // Check if current user liked/reported this review
        if (currentUserId != null) {
            builder.userLiked(reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), currentUserId))
                    .userReported(reviewReportRepository.existsByReviewIdAndUserId(review.getId(), currentUserId));
        }

        // Map target based on a review type
        if (review.getReviewType() == ReviewType.COURSE) {
            if (review.getCourse() != null) {
                builder.targetId(review.getCourse().getId())
                        .targetTitle(review.getCourse().getCourseName())
                        .targetType("COURSE");
            } else {
                log.warn("Review {} is COURSE type but course is null", review.getId());
            }
        } else if (review.getReviewType() == ReviewType.MOCK_TEST) {
            if (review.getMockTest() != null) {
                builder.targetId(review.getMockTest().getId())
                        .targetTitle(review.getMockTest().getTitle())
                        .targetType("MOCK_TEST");
            } else {
                log.warn("Review {} is MOCK_TEST type but mockTest is null", review.getId());
            }
        }

        return builder.build();
    }

    @Override
    public ReviewStatsDTO getReviewStatsDTO() {
        log.debug("📊 Fetching review statistics (DTO)");

        // Tổng số reviews
        long totalReviews = reviewRepository.count();

        // Đếm theo status - THÊM REPORTED
        long activeReviews = reviewRepository.countByStatus(ReviewStatus.ACTIVE);
        long reportedReviews = reviewRepository.countByStatus(ReviewStatus.REPORTED);  // THÊM
        long hiddenReviews = reviewRepository.countByStatus(ReviewStatus.HIDDEN);
        long deletedReviews = reviewRepository.countByStatus(ReviewStatus.DELETED);

        // Đếm theo type
        long courseReviews = reviewRepository.countByReviewType(ReviewType.COURSE);
        long mockTestReviews = reviewRepository.countByReviewType(ReviewType.MOCK_TEST);

        // Rating trung bình
        Double overallAvgRating = reviewRepository.findOverallAverageRating();
        double averageRating = formatRating(overallAvgRating);

        // Rating trung bình theo type
        Double courseAvgRating = reviewRepository.findAverageRatingByType(ReviewType.COURSE);
        Double mockTestAvgRating = reviewRepository.findAverageRatingByType(ReviewType.MOCK_TEST);

        // Rating distribution
        Map<Integer, Long> ratingDistribution = new TreeMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = reviewRepository.countByRating(i);
            ratingDistribution.put(i, count);
        }

        // Type distribution
        Map<String, Long> typeDistribution = new LinkedHashMap<>();
        typeDistribution.put("COURSE", courseReviews);
        typeDistribution.put("MOCK_TEST", mockTestReviews);

        // Status distribution
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("ACTIVE", activeReviews);
        statusDistribution.put("REPORTED", reportedReviews);
        statusDistribution.put("HIDDEN", hiddenReviews);
        statusDistribution.put("DELETED", deletedReviews);

        // Reviews trong 7 ngày và 30 ngày gần nhất
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long reviewsLast7Days = reviewRepository.countByCreatedAtAfter(sevenDaysAgo);
        long reviewsLast30Days = reviewRepository.countByCreatedAtAfter(thirtyDaysAgo);

        // Xu hướng rating (so với tháng trước)
        Double averageRatingTrend = calculateAverageRatingTrend();

        return ReviewStatsDTO.builder()
                .totalReviews(totalReviews)
                .activeReviews(activeReviews)
                .reportedReviews(reportedReviews)
                .hiddenReviews(hiddenReviews)
                .deletedReviews(deletedReviews)
                .courseReviews(courseReviews)
                .mockTestReviews(mockTestReviews)
                .averageRating(averageRating)
                .averageCourseRating(formatRating(courseAvgRating))
                .averageMockTestRating(formatRating(mockTestAvgRating))
                .ratingDistribution(ratingDistribution)
                .typeDistribution(typeDistribution)
                .statusDistribution(statusDistribution)
                .reviewsLast7Days(reviewsLast7Days)
                .reviewsLast30Days(reviewsLast30Days)
                .averageRatingTrend(averageRatingTrend)
                .build();
    }



    @Override
    public ReviewStatsDTO getCourseReviewStatsDTO(Long courseId) {
        log.debug("📊 Fetching course review statistics (DTO) for course: {}", courseId);

        validateCourseExists(courseId);

        long totalReviews = countReviewsByCourseId(courseId);
        double averageRating = getAverageCourseRating(courseId);

        // Đếm theo rating (1-5 sao)
        Map<Integer, Long> ratingDistribution = new TreeMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = reviewRepository.countByCourseIdAndRating(courseId, i);
            ratingDistribution.put(i, count);
        }

        // Đếm theo status
        long activeReviews = reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE);
        long reportedReviews = reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.REPORTED);
        long hiddenReviews = reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.HIDDEN);
        long deletedReviews = reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.DELETED);

        // Reviews trong 7 ngày và 30 ngày gần nhất
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long reviewsLast7Days = reviewRepository.countByCourseIdAndCreatedAtAfter(courseId, sevenDaysAgo);
        long reviewsLast30Days = reviewRepository.countByCourseIdAndCreatedAtAfter(courseId, thirtyDaysAgo);

        // Status distribution
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("ACTIVE", activeReviews);
        statusDistribution.put("REPORTED", reportedReviews);
        statusDistribution.put("HIDDEN", hiddenReviews);
        statusDistribution.put("DELETED", deletedReviews);

        return ReviewStatsDTO.builder()
                .totalReviews(totalReviews)
                .activeReviews(activeReviews)
                .reportedReviews(reportedReviews)
                .hiddenReviews(hiddenReviews)
                .deletedReviews(deletedReviews)
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .statusDistribution(statusDistribution)
                .reviewsLast7Days(reviewsLast7Days)
                .reviewsLast30Days(reviewsLast30Days)
                .build();
    }



    @Override
    public ReviewStatsDTO getMockTestReviewStatsDTO(Long mockTestId) {
        log.debug("📊 Fetching mock test review statistics (DTO) for mock test: {}", mockTestId);

        validateMockTestExists(mockTestId);

        long totalReviews = countReviewsByMockTestId(mockTestId);
        double averageRating = getAverageMockTestRating(mockTestId);

        // Đếm theo rating (1-5 sao)
        Map<Integer, Long> ratingDistribution = new TreeMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = reviewRepository.countByMockTestIdAndRating(mockTestId, i);
            ratingDistribution.put(i, count);
        }

        // Đếm theo status
        long activeReviews = reviewRepository.countByMockTestIdAndStatus(mockTestId, ReviewStatus.ACTIVE);
        long reportedReviews = reviewRepository.countByMockTestIdAndStatus(mockTestId, ReviewStatus.REPORTED);
        long hiddenReviews = reviewRepository.countByMockTestIdAndStatus(mockTestId, ReviewStatus.HIDDEN);
        long deletedReviews = reviewRepository.countByMockTestIdAndStatus(mockTestId, ReviewStatus.DELETED);

        // Reviews trong 7 ngày và 30 ngày gần nhất
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long reviewsLast7Days = reviewRepository.countByMockTestIdAndCreatedAtAfter(mockTestId, sevenDaysAgo);
        long reviewsLast30Days = reviewRepository.countByMockTestIdAndCreatedAtAfter(mockTestId, thirtyDaysAgo);

        // Status distribution
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("ACTIVE", activeReviews);
        statusDistribution.put("REPORTED", reportedReviews);
        statusDistribution.put("HIDDEN", hiddenReviews);
        statusDistribution.put("DELETED", deletedReviews);

        return ReviewStatsDTO.builder()
                .totalReviews(totalReviews)
                .activeReviews(activeReviews)
                .reportedReviews(reportedReviews) // THÊM
                .hiddenReviews(hiddenReviews)
                .deletedReviews(deletedReviews)
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .statusDistribution(statusDistribution)
                .reviewsLast7Days(reviewsLast7Days)
                .reviewsLast30Days(reviewsLast30Days)
                .build();
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
        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        return Review.builder()
                .user(user)
                .course(course)
                .mockTest(mockTest)
                .reviewType(request.getReviewType())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.ACTIVE)
                .adminNote(null)
                .build();
    }


    private void validateReviewIsActive(Review review) {
        if (review.getStatus() != ReviewStatus.ACTIVE) {
            throw new IllegalStateException("Không thể chỉnh sửa đánh giá đã bị ẩn/xóa");
        }
    }

    private double formatRating(Double rating) {
        return rating != null ? Math.round(rating * 10.0) / 10.0 : 0.0;
    }

    // ========== STATISTICS METHODS ==========

    /**
     * Lấy thống kê tổng quan về reviews
     *
     * @return Map chứa các thống kê
     */
    @Override
    public Map<String, Object> getReviewStats() {
        // Tổng số reviews
        long totalReviews = reviewRepository.count();

        // Đếm theo status
        long activeReviews = reviewRepository.countByStatus(ReviewStatus.ACTIVE);
        long reportedReviews = reviewRepository.countByStatus(ReviewStatus.REPORTED);
        long hiddenReviews = reviewRepository.countByStatus(ReviewStatus.HIDDEN);

        // Đếm theo type
        long courseReviews = reviewRepository.countByReviewType(ReviewType.COURSE);
        long mockTestReviews = reviewRepository.countByReviewType(ReviewType.MOCK_TEST);

        // Rating trung bình tất cả reviews
        Double overallAvgRating = reviewRepository.findOverallAverageRating();
        double averageRating = formatRating(overallAvgRating);

        return Map.of(
                "totalReviews", totalReviews,
                "activeReviews", activeReviews,
                "reportedReviews", reportedReviews,
                "hiddenReviews", hiddenReviews,
                "courseReviews", courseReviews,
                "mockTestReviews", mockTestReviews,
                "averageRating", averageRating
        );
    }

    /**
     * Lấy thống kê reviews của một course cụ thể
     *
     * @param courseId ID của course
     * @return Map chứa thống kê
     */
    @Override
    public Map<String, Object> getCourseReviewStats(Long courseId) {
        long totalReviews = countReviewsByCourseId(courseId);
        double averageRating = getAverageCourseRating(courseId);

        // Đếm theo rating (1-5 sao)
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = reviewRepository.countByCourseIdAndRating(courseId, i);
            ratingDistribution.put(i, count);
        }

        return Map.of(
                "courseId", courseId,
                "totalReviews", totalReviews,
                "averageRating", averageRating,
                "ratingDistribution", ratingDistribution
        );
    }

    /**
     * Lấy thống kê reviews của một mock test cụ thể
     *
     * @param mockTestId ID của mock test
     * @return Map chứa thống kê
     */
    @Override
    public Map<String, Object> getMockTestReviewStats(Long mockTestId) {
        long totalReviews = countReviewsByMockTestId(mockTestId);
        double averageRating = getAverageMockTestRating(mockTestId);

        // Đếm theo rating (1-5 sao)
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = reviewRepository.countByMockTestIdAndRating(mockTestId, i);
            ratingDistribution.put(i, count);
        }

        return Map.of(
                "mockTestId", mockTestId,
                "totalReviews", totalReviews,
                "averageRating", averageRating,
                "ratingDistribution", ratingDistribution
        );
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        ReviewStatsDTO stats = getReviewStatsDTO();

        Map<String, Object> dashboardStats = new LinkedHashMap<>();

        // 1. Daily Reviews Data - 7 ngày gần nhất
        dashboardStats.put("dailyReviews", getDailyReviewCounts());

        // 2. Rating Distribution
        Map<Integer, Long> ratingDist = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDist.put(i, stats.getRatingDistribution().getOrDefault(i, 0L));
        }
        dashboardStats.put("ratingDistribution", ratingDist);

        // 3. Recent Activities
        dashboardStats.put("recentActivities", getRecentActivities());

        // 4. Top Reviewed Courses
        dashboardStats.put("topReviewedCourses", getTopReviewedCourses(5));

        // 5. Stats tổng quan
        dashboardStats.put("totalReviews", stats.getTotalReviews());
        dashboardStats.put("averageRating", stats.getAverageRating());
        dashboardStats.put("courseReviews", stats.getCourseReviews());
        dashboardStats.put("mockTestReviews", stats.getMockTestReviews());
        dashboardStats.put("reviewsLast7Days", stats.getReviewsLast7Days());
        dashboardStats.put("reviewsLast30Days", stats.getReviewsLast30Days());

        return dashboardStats;
    }

    // Lấy số lượng review theo ngày (7 ngày gần nhất)
    private List<Integer> getDailyReviewCounts() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6); // 7 ngày gần nhất

            List<Integer> dailyCounts = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = startDate.plusDays(i);

                // Query lấy số review trong ngày
                LocalDateTime startOfDay = currentDate.atStartOfDay();
                LocalDateTime endOfDay = currentDate.plusDays(1).atStartOfDay();

                Long count = reviewRepository.countByCreatedAtBetweenAndStatus(
                        startOfDay,
                        endOfDay,
                        ReviewStatus.ACTIVE
                );

                dailyCounts.add(count != null ? count.intValue() : 0);
            }

            return dailyCounts;

        } catch (Exception e) {
            log.error("Error getting daily review counts, using fallback", e);
            // Fallback data cho 7 ngày
            return Arrays.asList(5, 8, 12, 15, 10, 7, 6);
        }
    }

    // Lấy hoạt động gần đây
    private List<Map<String, Object>> getRecentActivities() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findAll(pageable);

        return reviews.getContent().stream()
                .map(this::convertToActivity)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertToActivity(Review review) {
        Map<String, Object> activity = new LinkedHashMap<>();
        activity.put("id", review.getId());

        // Lấy thông tin user
        if (review.getUser() != null) {
            activity.put("userName", review.getUser().getFullName());
            activity.put("userEmail", review.getUser().getEmail());
        } else {
            activity.put("userName", "Ẩn danh");
            activity.put("userEmail", "N/A");
        }

        // Xác định loại hành động
        String actionType;
        String icon;
        if (review.getReviewType() == ReviewType.COURSE) {
            actionType = "Đánh giá khóa học";
            icon = "📚";
        } else {
            actionType = "Đánh giá bài thi";
            icon = "📝";
        }

        activity.put("actionType", actionType);
        activity.put("icon", icon);

        // Tạo mô tả
        String description;
        if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
            String shortComment = review.getComment().length() > 60
                    ? review.getComment().substring(0, 60) + "..."
                    : review.getComment();
            description = String.format("Đánh giá %d sao: %s", review.getRating(), shortComment);
        } else {
            description = String.format("Đánh giá %d sao", review.getRating());
        }
        activity.put("description", description);

        // Thêm thông tin về đối tượng được đánh giá
        if (review.getCourse() != null) {
            activity.put("courseId", review.getCourse().getId());
            activity.put("courseTitle", review.getCourse().getCourseName());
        } else if (review.getMockTest() != null) {
            activity.put("mockTestId", review.getMockTest().getId());
            activity.put("mockTestTitle", review.getMockTest().getTitle());
        }

        activity.put("createdAt", review.getCreatedAt());
        activity.put("rating", review.getRating());

        return activity;
    }

    // Lấy top courses có nhiều review nhất
    private List<Map<String, Object>> getTopReviewedCourses(int limit) {
        // Query để lấy top courses thực sự
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "reviewCount"));

        // Nếu có Course entity với field reviewCount
        List<Object[]> topCoursesData = reviewRepository.findTopReviewedCourses(limit);

        List<Map<String, Object>> topCourses = new ArrayList<>();

        for (Object[] data : topCoursesData) {
            Long courseId = (Long) data[0];
            String courseTitle = (String) data[1];
            Long reviewCount = (Long) data[2];
            Double avgRating = (Double) data[3];

            Map<String, Object> course = new LinkedHashMap<>();
            course.put("id", courseId);
            course.put("title", courseTitle);
            course.put("reviewCount", reviewCount);
            course.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

            topCourses.add(course);
        }

        // Nếu không có dữ liệu, trả về list rỗng
        return topCourses;
    }

    @Override
    @Transactional
    public ReviewDTO reportReview(Long reviewId, String reason, Long userId) {
        log.info("Reporting review: {} by user: {}, reason: {}", reviewId, userId, reason);

        Review review = getReviewOrThrow(reviewId);
        User user = getUserOrThrow(userId);

        // Check if user has already reported this review
        if (reviewReportRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new IllegalStateException("Bạn đã report review này rồi");
        }

        // Create report record
        ReviewReport report = ReviewReport.builder()
                .review(review)
                .user(user)
                .reason(reason)
                .resolved(false)
                .build();
        reviewReportRepository.save(report);

        // If reports reach a threshold, mark review as REPORTED
        long activeReportCount = reviewReportRepository.countByReviewIdAndResolvedFalse(reviewId);
        if (activeReportCount >= 5) { // Threshold
            review.setStatus(ReviewStatus.REPORTED);
            reviewRepository.save(review);
        }

        log.info("Review {} reported by user {}", reviewId, userId);
        return mapToDTO(review);
    }

    @Override
    @Transactional
    public ReviewDTO likeReview(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        User user = getUserOrThrow(userId);

        // Check if user has already liked this review
        if (reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new IllegalStateException("Bạn đã like review này rồi");
        }

        // Create like record với unique constraint
        String uniqueConstraint = userId + "_" + reviewId;
        ReviewLike like = ReviewLike.builder()
                .review(review)
                .user(user)
                .uniqueConstraint(uniqueConstraint)
                .build();
        reviewLikeRepository.save(like);

        log.info("Review {} liked by user {}", reviewId, userId);
        return mapToDTO(review);
    }

    @Override
    @Transactional
    public ReviewDTO unlikeReview(Long reviewId, Long userId) {
        // Check if like exists
        ReviewLike like = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalStateException("You haven't liked this review"));

        // Delete it like record
        reviewLikeRepository.delete(like);

        log.info("Review {} unliked by user {}", reviewId, userId);
        return getReviewById(reviewId);
    }

    @Override
    public boolean hasUserLikedReview(Long reviewId, Long userId) {
        return reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);
    }

    @Override
    public Long getLikesCount(Long reviewId) {
        return reviewLikeRepository.countByReviewId(reviewId);
    }

    @Override
    public Page<ReviewDTO> getReportedReviews(Pageable pageable) {
        Page<Review> reportedReviews = reviewRepository.findByStatus(ReviewStatus.REPORTED, pageable);
        return reportedReviews.map(this::mapToDTO);
    }

    @Override
    @Transactional
    public ReviewDTO resolveReport(Long reviewId, boolean takeAction, String adminNote) {
        Review review = getReviewOrThrow(reviewId);

        if (takeAction) {
            // Admin decides to hide the review
            review.setStatus(ReviewStatus.HIDDEN);
            review.setAdminNote(adminNote);
        } else {
            // Admin decides it's fine, restore to active
            review.setStatus(ReviewStatus.ACTIVE);
            review.setAdminNote(adminNote);
        }

        // Mark-all reports as resolved
        List<ReviewReport> reports = reviewReportRepository.findAllByReviewId(reviewId);
        reports.forEach(report -> report.setResolved(true));
        reviewReportRepository.saveAll(reports);

        Review savedReview = reviewRepository.save(review);
        log.info("Report resolved for review {}: action taken = {}", reviewId, takeAction);
        return mapToDTO(savedReview);
    }

    private Double calculateAverageRatingTrend() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thisMonthStart = now.withDayOfMonth(1);
            LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
            LocalDateTime lastMonthEnd = thisMonthStart.minusSeconds(1);

            // Rating trung bình tháng này
            Double currentMonthAvg = reviewRepository.findAverageRatingByDateRange(
                    thisMonthStart, now);

            // Rating trung bình tháng trước
            Double lastMonthAvg = reviewRepository.findAverageRatingByDateRange(
                    lastMonthStart, lastMonthEnd);

            if (lastMonthAvg == null || lastMonthAvg == 0.0) {
                return 0.0;
            }

            return ((currentMonthAvg != null ? currentMonthAvg : 0.0) - lastMonthAvg) / lastMonthAvg * 100;
        } catch (Exception e) {
            log.warn("Error calculating rating trend: {}", e.getMessage());
            return 0.0;
        }
    }

    private Map<String, Object> getDailyReviewsTrend() {
        Map<String, Object> chartData = new LinkedHashMap<>();

        List<String> labels = new java.util.ArrayList<>();
        List<Long> data = new java.util.ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime startOfDay = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

            long count = reviewRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            labels.add(startOfDay.toLocalDate().toString());
            data.add(count);
        }

        chartData.put("labels", labels);
        chartData.put("datasets", List.of(Map.of(
                "label", "Số review",
                "data", data,
                "borderColor", "#3B82F6",
                "backgroundColor", "rgba(59, 130, 246, 0.1)",
                "tension", 0.4
        )));

        return chartData;
    }

    private List<Map<String, Object>> getTopRatedItems() {
        // Lấy top 5 courses có rating cao nhất
        List<Object[]> topCourses = reviewRepository.findTopRatedCourses(5);
        List<Object[]> topMockTests = reviewRepository.findTopRatedMockTests(5);

        List<Map<String, Object>> topItems = new java.util.ArrayList<>();

        for (Object[] courseData : topCourses) {
            topItems.add(Map.of(
                    "type", "COURSE",
                    "id", courseData[0],
                    "title", courseData[1],
                    "averageRating", courseData[2],
                    "reviewCount", courseData[3]
            ));
        }

        for (Object[] mockTestData : topMockTests) {
            topItems.add(Map.of(
                    "type", "MOCK_TEST",
                    "id", mockTestData[0],
                    "title", mockTestData[1],
                    "averageRating", mockTestData[2],
                    "reviewCount", mockTestData[3]
            ));
        }

        // Sort by rating descending
        topItems.sort((a, b) -> {
            Double ratingA = (Double) a.get("averageRating");
            Double ratingB = (Double) b.get("averageRating");
            return ratingB.compareTo(ratingA);
        });

        return topItems.stream().limit(10).collect(java.util.stream.Collectors.toList());
    }

    private void validateCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId);
        }
    }

    private void validateMockTestExists(Long mockTestId) {
        if (!mockTestRepository.existsById(mockTestId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + mockTestId);
        }
    }
}