package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.review.ReviewRequest;
import korastudy.be.dto.request.review.UpdateReviewStatusRequest;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.dto.response.review.ReviewStatsDTO;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.payload.response.PagedResponse;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    // ====================================================================
    // 🟢 PUBLIC ENDPOINTS (Không cần đăng nhập)
    // ====================================================================

    // ========== COURSE REVIEWS ==========

    /**
     * 🟢 PUBLIC: Lấy danh sách review của một khóa học (phân trang)
     * Dùng cho trang chi tiết khóa học
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<PagedResponse<ReviewDTO>> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("📥 GET course reviews for courseId: {}", courseId);

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.getCourseReviewsWithPagination(courseId, pageable);

            log.debug("✅ Found {} reviews for courseId: {}", reviews.getTotalElements(), courseId);

            return ResponseEntity.ok(new PagedResponse<>(
                    reviews.getContent(),
                    page,
                    size,
                    reviews.getTotalElements(),
                    reviews.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("❌ Error getting course reviews for courseId {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🟢 PUBLIC: Lấy rating trung bình của khóa học
     * Dùng để hiển thị rating trên card khóa học
     */
    @GetMapping("/courses/{courseId}/average-rating")
    public ResponseEntity<Double> getCourseAverageRating(@PathVariable Long courseId) {
        try {
            double averageRating = reviewService.getAverageCourseRating(courseId);
            log.debug("📊 Average rating for courseId {}: {}", courseId, averageRating);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            log.error("❌ Error getting average rating for courseId {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🟢 PUBLIC: Đếm số review của khóa học
     * Dùng để hiển thị số lượng review
     */
    @GetMapping("/courses/{courseId}/count")
    public ResponseEntity<Long> countCourseReviews(@PathVariable Long courseId) {
        try {
            long count = reviewService.countReviewsByCourseId(courseId);
            log.debug("🔢 Review count for courseId {}: {}", courseId, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("❌ Error counting reviews for courseId {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🟢 PUBLIC: Thống kê review của course (rating distribution)
     * Dùng cho biểu đồ phân bố rating trên trang khóa học
     */
    @GetMapping("/courses/{courseId}/stats")
    public ResponseEntity<?> getCourseReviewStats(@PathVariable Long courseId) {
        try {
            Map<String, Object> stats = reviewService.getCourseReviewStats(courseId);
            log.debug("📊 Course review stats for courseId {}: {}", courseId, stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Error getting course review stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get course review stats"));
        }
    }

    /**
     * 🟢 PUBLIC: Lấy thống kê DTO chi tiết của course
     */
    @GetMapping("/courses/{courseId}/stats/dto")
    public ResponseEntity<?> getCourseReviewStatsDTO(@PathVariable Long courseId) {
        try {
            ReviewStatsDTO stats = reviewService.getCourseReviewStatsDTO(courseId);
            log.debug("📊 Course review stats DTO for courseId {}: {}", courseId, stats.getTotalReviews());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Error getting course review stats DTO: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get course review stats"));
        }
    }



    // ========== MOCK TEST REVIEWS ==========

    /**
     * 🟢 PUBLIC: Lấy danh sách review của một bài thi (phân trang)
     */
    @GetMapping("/mock-tests/{mockTestId}")
    public ResponseEntity<PagedResponse<ReviewDTO>> getMockTestReviews(
            @PathVariable Long mockTestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("📥 GET mock test reviews for mockTestId: {}", mockTestId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReviewDTO> reviews = reviewService.getMockTestReviewsWithPagination(mockTestId, pageable);

            log.debug("✅ Found {} reviews for mockTestId: {}", reviews.getTotalElements(), mockTestId);

            return ResponseEntity.ok(new PagedResponse<>(
                    reviews.getContent(),
                    page,
                    size,
                    reviews.getTotalElements(),
                    reviews.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("❌ Error getting mock test reviews for mockTestId {}: {}", mockTestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🟢 PUBLIC: Lấy rating trung bình của bài thi
     */
    @GetMapping("/mock-tests/{mockTestId}/average-rating")
    public ResponseEntity<Double> getMockTestAverageRating(@PathVariable Long mockTestId) {
        try {
            double averageRating = reviewService.getAverageMockTestRating(mockTestId);
            log.debug("📊 Average rating for mockTestId {}: {}", mockTestId, averageRating);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            log.error("❌ Error getting average rating for mockTestId {}: {}", mockTestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🟢 PUBLIC: Đếm số review của bài thi
     */
    @GetMapping("/mock-tests/{mockTestId}/count")
    public ResponseEntity<Long> countMockTestReviews(@PathVariable Long mockTestId) {
        try {
            long count = reviewService.countReviewsByMockTestId(mockTestId);
            log.debug("🔢 Review count for mockTestId {}: {}", mockTestId, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("❌ Error counting reviews for mockTestId {}: {}", mockTestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🟢 PUBLIC: Thống kê review của mock test
     */
    @GetMapping("/mock-tests/{mockTestId}/stats")
    public ResponseEntity<?> getMockTestReviewStats(@PathVariable Long mockTestId) {
        try {
            Map<String, Object> stats = reviewService.getMockTestReviewStats(mockTestId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Error getting mock test review stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get mock test review stats"));
        }
    }

    /**
     * 🟢 PUBLIC: Lấy thống kê DTO chi tiết của mock test
     */
    @GetMapping("/mock-tests/{mockTestId}/stats/dto")
    public ResponseEntity<?> getMockTestReviewStatsDTO(@PathVariable Long mockTestId) {
        try {
            ReviewStatsDTO stats = reviewService.getMockTestReviewStatsDTO(mockTestId);
            log.debug("📊 Mock test review stats DTO for mockTestId {}: {}", mockTestId, stats.getTotalReviews());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Error getting mock test review stats DTO: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get mock test review stats"));
        }
    }

    // ========== SINGLE REVIEW ==========

    /**
     * 🟢 PUBLIC: Xem chi tiết một review
     * Dùng khi click vào review để xem full content
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long reviewId) {
        log.debug("🔍 Getting review detail ID: {}", reviewId);

        try {
            ReviewDTO review = reviewService.getReviewById(reviewId);
            log.debug("✅ Retrieved review detail for ID: {}", reviewId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("❌ Error getting review detail {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get review detail"));
        }
    }

    // ====================================================================
    // 🔵 USER ENDPOINTS (Yêu cầu đăng nhập - ROLE_USER)
    // ====================================================================

    // ========== CREATE & MANAGE REVIEWS ==========

    /**
     * 🔵 USER: Tạo review mới
     * Dùng khi user viết review trên trang khóa học/bài thi
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("📝 Creating review for type: {}, targetId: {}",
                request.getReviewType(), request.getTargetId());

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.warn("❌ Unauthorized attempt to create review");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            log.debug("👤 User ID for review creation: {}", userId);
            ReviewDTO createdReview = reviewService.addReview(userId, request);

            log.info("✅ Review created successfully with ID: {}", createdReview.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Business rule violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error creating review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create review"));
        }
    }

    /**
     * 🔵 USER: Sửa review của mình
     * Dùng trong trang cá nhân, popup chỉnh sửa review
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {

        log.info("✏️ Updating review ID: {}", reviewId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            ReviewDTO updatedReview = reviewService.updateReview(userId, reviewId, request);
            log.info("✅ Review {} updated successfully", reviewId);

            return ResponseEntity.ok(updatedReview);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Permission denied or invalid state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error updating review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update review"));
        }
    }

    /**
     * 🔵 USER: Xóa review của mình
     * Dùng trong trang cá nhân, nút xóa review
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMyReview(@PathVariable Long reviewId) {
        log.info("🗑️ Deleting review ID: {}", reviewId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            reviewService.deleteReview(userId, reviewId);
            log.info("✅ Review {} deleted successfully", reviewId);

            return ResponseEntity.ok(ApiSuccess.of("Xóa đánh giá thành công"));

        } catch (IllegalStateException e) {
            log.warn("⚠️ Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error deleting review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete review"));
        }
    }

    // ========== USER'S REVIEWS ==========

    /**
     * 🔵 USER: Lấy tất cả review của user hiện tại
     * Dùng cho trang cá nhân "Đánh giá của tôi"
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("📚 Getting my reviews, page: {}, size: {}", page, size);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReviewDTO> reviews = reviewService.getUserReviewsWithPagination(userId, pageable);

            log.debug("✅ Found {} of my reviews", reviews.getTotalElements());

            return ResponseEntity.ok(new PagedResponse<>(
                    reviews.getContent(),
                    page,
                    size,
                    reviews.getTotalElements(),
                    reviews.getTotalPages()
            ));

        } catch (Exception e) {
            log.error("❌ Error getting my reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get reviews"));
        }
    }

    /**
     * 🔵 USER: Kiểm tra user đã review chưa
     * Dùng để ẩn/hiện nút "Viết đánh giá" trên trang khóa học/bài thi
     */
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkUserReview(
            @RequestParam String reviewType,
            @RequestParam Long targetId) {

        log.debug("🔍 Checking if user reviewed {} {}: {}", reviewType, targetId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            boolean hasReviewed;
            if ("COURSE".equalsIgnoreCase(reviewType)) {
                hasReviewed = reviewService.hasUserReviewedCourse(userId, targetId);
            } else if ("MOCK_TEST".equalsIgnoreCase(reviewType)) {
                hasReviewed = reviewService.hasUserReviewedMockTest(userId, targetId);
            } else {
                log.warn("⚠️ Invalid review type: {}", reviewType);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid review type. Use COURSE or MOCK_TEST"));
            }

            log.debug("✅ User {} has reviewed {} {}: {}", userId, reviewType, targetId, hasReviewed);
            return ResponseEntity.ok(Map.of("hasReviewed", hasReviewed));

        } catch (Exception e) {
            log.error("❌ Error checking user review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check review status"));
        }
    }

    // ========== LIKE & REPORT INTERACTIONS ==========

    /**
     * 🔵 USER: Like một review
     * Dùng cho nút like trên card review
     */
    @PostMapping("/{reviewId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> likeReview(@PathVariable Long reviewId) {
        log.info("👍 Liking review ID: {}", reviewId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            ReviewDTO review = reviewService.likeReview(reviewId, userId);
            log.info("✅ Review {} liked by user {}", reviewId, userId);

            return ResponseEntity.ok(review);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Already liked: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error liking review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to like review"));
        }
    }

    /**
     * 🔵 USER: Bỏ like một review
     */
    @DeleteMapping("/{reviewId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> unlikeReview(@PathVariable Long reviewId) {
        log.info("👎 Unliking review ID: {}", reviewId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            ReviewDTO review = reviewService.unlikeReview(reviewId, userId);
            log.info("✅ Review {} unliked by user {}", reviewId, userId);

            return ResponseEntity.ok(review);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Not liked: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error unliking review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unlike review"));
        }
    }

    /**
     * 🔵 USER: Kiểm tra đã like review chưa
     * Dùng để hiển thị trạng thái nút like (filled/outline)
     */
    @GetMapping("/{reviewId}/like/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkUserLike(@PathVariable Long reviewId) {
        log.debug("🔍 Checking if user liked review: {}", reviewId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            boolean hasLiked = reviewService.hasUserLikedReview(reviewId, userId);
            log.debug("✅ User {} has liked review {}: {}", userId, reviewId, hasLiked);

            return ResponseEntity.ok(Map.of("hasLiked", hasLiked));

        } catch (Exception e) {
            log.error("❌ Error checking user like: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check like status"));
        }
    }

    /**
     * 🔵 USER: Report một review
     * Dùng cho nút "Báo cáo vi phạm" trên card review
     */
    @PostMapping("/{reviewId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reportReview(
            @PathVariable Long reviewId,
            @RequestParam String reason) {

        log.info("⚠️ Reporting review ID: {}, reason: {}", reviewId, reason);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            ReviewDTO review = reviewService.reportReview(reviewId, reason, userId);
            log.info("✅ Review {} reported by user {}", reviewId, userId);

            return ResponseEntity.ok(review);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Already reported: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error reporting review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to report review"));
        }
    }

    /**
     * 🔵 USER: Kiểm tra đã report review chưa
     */
    @GetMapping("/{reviewId}/report/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkUserReport(@PathVariable Long reviewId) {
        log.debug("🔍 Checking if user reported review: {}", reviewId);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            // Giả sử có phương thức này trong service
            // boolean hasReported = reviewService.hasUserReportedReview(reviewId, userId);
            // Tạm thời trả về false
            boolean hasReported = false;

            log.debug("✅ User {} has reported review {}: {}", userId, reviewId, hasReported);
            return ResponseEntity.ok(Map.of("hasReported", hasReported));

        } catch (Exception e) {
            log.error("❌ Error checking user report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check report status"));
        }
    }

    // ====================================================================
    // 👑 ADMIN ENDPOINTS (Yêu cầu role ADMIN)
    // ====================================================================

    // ========== ADMIN DASHBOARD & STATS ==========

    /**
     * 👑 ADMIN: Lấy thống kê tổng quan (DTO version)
     * Dùng cho dashboard admin
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewStats() {
        log.debug("📊 Admin getting review stats");

        try {
            ReviewStatsDTO stats = reviewService.getReviewStatsDTO();
            log.debug("✅ Admin retrieved review stats - Total: {}", stats.getTotalReviews());
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Admin error getting review stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get review stats"));
        }
    }

    /**
     * 👑 ADMIN: Lấy dashboard stats với chart data
     * Dùng cho dashboard với charts
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.debug("📈 Admin getting dashboard stats");

        try {
            Map<String, Object> dashboardStats = reviewService.getDashboardStats();

            // Thêm timestamp để biết khi nào data được tạo
            dashboardStats.put("generatedAt", LocalDateTime.now().toString());

            // Debug log chi tiết
            log.info("✅ Dashboard stats generated successfully");

            // Chỉ cần log các key chính, không cần check type
            log.info("  - dailyReviews: {}", dashboardStats.get("dailyReviews"));
            log.info("  - ratingDistribution: {}", dashboardStats.get("ratingDistribution"));
            log.info("  - recentActivities: {}", dashboardStats.get("recentActivities"));
            log.info("  - topReviewedCourses: {}", dashboardStats.get("topReviewedCourses"));
            log.info("  - reviewsLast7Days: {}", dashboardStats.get("reviewsLast7Days"));

            return ResponseEntity.ok(dashboardStats);

        } catch (Exception e) {
            log.error("❌ Admin error getting dashboard stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to get dashboard stats",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now().toString()
                    ));
        }
    }

    /**
     * 👑 ADMIN: Lấy thống kê legacy (Map version - backward compatibility)
     */
    @GetMapping("/admin/stats/legacy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewStatsLegacy() {
        log.debug("📊 Admin getting legacy review stats");

        try {
            Map<String, Object> stats = reviewService.getReviewStats();
            log.debug("✅ Admin retrieved legacy review stats");
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Admin error getting legacy review stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get review stats"));
        }
    }

    // ========== ADMIN REVIEW MANAGEMENT ==========

    /**
     * 👑 ADMIN: Lấy tất cả review với filter
     * Dùng cho trang quản lý review với bộ lọc
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) ReviewStatus status) {

        log.debug("👨‍💼 Admin getting all reviews, filter: type={}, status={}", targetType, status);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            // Validate targetType
            if (targetType != null && !targetType.trim().isEmpty()) {
                try {
                    ReviewType.valueOf(targetType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("⚠️ Invalid review type provided: {}", targetType);
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid review type. Use: COURSE, MOCK_TEST"));
                }
            }

            Page<ReviewDTO> reviews = reviewService.getAllReviews(targetType, status, pageable);
            log.debug("✅ Admin found {} reviews", reviews.getTotalElements());

            return ResponseEntity.ok(new PagedResponse<>(
                    reviews.getContent(),
                    page,
                    size,
                    reviews.getTotalElements(),
                    reviews.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("❌ Admin error getting all reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get reviews"));
        }
    }

    /**
     * 👑 ADMIN: Lấy review bị report
     * Dùng cho tab "Review bị báo cáo"
     */
    @GetMapping("/admin/reported")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReportedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("🚨 Admin getting reported reviews");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReviewDTO> reviews = reviewService.getReportedReviews(pageable);

            log.debug("✅ Admin found {} reported reviews", reviews.getTotalElements());

            return ResponseEntity.ok(new PagedResponse<>(
                    reviews.getContent(),
                    page,
                    size,
                    reviews.getTotalElements(),
                    reviews.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("❌ Admin error getting reported reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get reported reviews"));
        }
    }

    /**
     * 👑 ADMIN: Xem chi tiết một review (admin version)
     * Có thêm thông tin reports, likes chi tiết
     */
    @GetMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewDetailAdmin(@PathVariable Long reviewId) {
        log.debug("👨‍💼 Admin getting FULL review detail ID: {}", reviewId);

        try {
            Long adminId = getCurrentUserId();
            if (adminId == null) {
                log.warn("❌ Admin ID not found in security context");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            log.debug("👤 Admin ID: {}, Review ID: {}", adminId, reviewId);

            // Dùng phương thức getReviewDetailForAdmin để lấy thông tin đầy đủ
            ReviewDTO review = reviewService.getReviewDetailForAdmin(reviewId, adminId);

            log.debug("✅ Admin retrieved FULL review detail for ID: {}", reviewId);
            return ResponseEntity.ok(review);

        } catch (ResourceNotFoundException e) {
            log.warn("⚠️ Review not found: {}", reviewId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy đánh giá"));
        } catch (IllegalStateException e) {
            log.warn("⚠️ Admin permission issue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Admin error getting review detail {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get review detail"));
        }
    }

    // ========== ADMIN REVIEW ACTIONS ==========

    /**
     * 👑 ADMIN: Cập nhật trạng thái review (ẩn/xóa/active)
     * Dùng cho nút "Ẩn review", "Khôi phục", "Xóa vĩnh viễn"
     */
    @PutMapping("/admin/{reviewId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable Long reviewId,
            @RequestBody UpdateReviewStatusRequest request) {

        log.info("👨‍💼 Admin updating review {} status to {}", reviewId, request.getStatus());

        try {
            ReviewDTO updatedReview = reviewService.updateReviewStatus(reviewId, request);
            log.info("✅ Admin updated review {} status successfully", reviewId);
            return ResponseEntity.ok(updatedReview);
        } catch (Exception e) {
            log.error("❌ Admin error updating review status {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update review status"));
        }
    }

    /**
     * 👑 ADMIN: Xử lý report (resolve)
     * Dùng trong modal xử lý report (duyệt/từ chối report)
     */
    @PutMapping("/admin/{reviewId}/resolve-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolveReport(
            @PathVariable Long reviewId,
            @RequestParam boolean takeAction,
            @RequestParam(required = false) String adminNote) {

        log.info("👨‍💼 Admin resolving report for review {}: takeAction={}", reviewId, takeAction);

        try {
            ReviewDTO review = reviewService.resolveReport(reviewId, takeAction, adminNote);
            log.info("✅ Admin resolved report for review {}", reviewId);

            String message = takeAction
                    ? "Đã ẩn review và đánh dấu đã xử lý"
                    : "Đã khôi phục review và đánh dấu đã xử lý";

            return ResponseEntity.ok(Map.of(
                    "review", review,
                    "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Admin error resolving report for review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to resolve report"));
        }
    }

    /**
     * 👑 ADMIN: Xóa review (hard delete)
     * Dùng cho nút "Xóa vĩnh viễn" trong admin panel
     */
    @DeleteMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Hoặc hasRole('ROLE_ADMIN') tùy cách bạn config
    public ResponseEntity<?> deleteReviewAsAdmin(@PathVariable Long reviewId) {
        log.info("👨‍💼 Admin deleting review ID: {}", reviewId);

        // DEBUG: Kiểm tra authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("DEBUG - Authentication: {}", auth);
        log.info("DEBUG - Principal: {}", auth.getPrincipal());
        log.info("DEBUG - Authorities: {}", auth.getAuthorities());
        log.info("DEBUG - Is Authenticated: {}", auth.isAuthenticated());

        try {
            Long adminId = getCurrentUserId();
            log.info("DEBUG - Admin ID from context: {}", adminId);

            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            // DEBUG: Kiểm tra trước khi gọi service
            log.info("DEBUG - Calling deleteReviewAsAdmin with adminId: {}, reviewId: {}", adminId, reviewId);

            // ✅ Gọi phương thức mới deleteReviewAsAdmin thay vì deleteReview
            reviewService.deleteReviewAsAdmin(adminId, reviewId);

            log.info("✅ Admin deleted review {} successfully", reviewId);

            return ResponseEntity.ok(Map.of("message", "Xóa đánh giá thành công"));
        } catch (IllegalStateException e) {
            log.warn("⚠️ Admin permission issue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Admin error deleting review {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete review"));
        }
    }

    // ====================================================================
    // 🔧 HELPER METHODS
    // ====================================================================

    /**
     * Lấy userId từ SecurityContext
     * @return User ID hoặc null nếu không authenticated
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                log.debug("🔒 User not authenticated");
                return null;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof AccountDetailsImpl) {
                Long userId = ((AccountDetailsImpl) principal).getId();
                log.debug("✅ Got userId from AccountDetailsImpl: {}", userId);
                return userId;
            }

            // Nếu principal là String (username), cần query DB để lấy ID
            if (principal instanceof String) {
                String username = (String) principal;
                log.warn("⚠️ Principal is String, need to fetch user ID from DB: {}", username);
                // Implement: return userRepository.findByUsername(username).map(User::getId).orElse(null);
                return null;
            }

            log.error("❌ Unexpected principal type: {}",
                    principal != null ? principal.getClass().getName() : "null");
            return null;

        } catch (Exception e) {
            log.error("❌ Error getting current user ID: {}", e.getMessage(), e);
            return null;
        }
    }
}