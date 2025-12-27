package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.review.ReviewRequest;
import korastudy.be.dto.request.review.UpdateReviewStatusRequest;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.entity.Enum.ReviewStatus;
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

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    // ====================================================================
    // PUBLIC ENDPOINTS (Không cần đăng nhập)
    // ====================================================================

    /**
     * Lấy tất cả review của một khóa học
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
     * Lấy tất cả review của một bài thi
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
     * Lấy rating trung bình của khóa học
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
     * Lấy rating trung bình của bài thi
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
     * Đếm số review của khóa học
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
     * Đếm số review của bài thi
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

    // ====================================================================
    // USER ENDPOINTS (Yêu cầu đăng nhập)
    // ====================================================================

    /**
     * Tạo review mới
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
     * Sửa review của mình
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
     * Xóa review của mình
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

    /**
     * Lấy tất cả review của user hiện tại
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
     * Kiểm tra user đã review chưa
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

    // ====================================================================
    // ADMIN ENDPOINTS (Yêu cầu role ADMIN)
    // ====================================================================

    /**
     * ADMIN: Lấy tất cả review với filter
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
     * ADMIN: Cập nhật trạng thái review (ẩn/xóa/active)
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
     * ADMIN: Xóa review (hard delete)
     */
    @DeleteMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReviewAsAdmin(@PathVariable Long reviewId) {
        log.info("👨‍💼 Admin deleting review ID: {}", reviewId);

        try {
            Long adminId = getCurrentUserId();
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            reviewService.deleteReview(adminId, reviewId);
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

    /**
     * ADMIN: Lấy review bị report
     */
    @GetMapping("/admin/reported")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReportedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("🚨 Admin getting reported reviews");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReviewDTO> reviews = reviewService.getAllReviews(null, ReviewStatus.REPORTED, pageable);

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
     * ADMIN: Xem chi tiết một review
     */
    @GetMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long reviewId) {
        log.debug("👨‍💼 Admin getting review detail ID: {}", reviewId);

        try {
            ReviewDTO review = reviewService.getReviewById(reviewId);
            log.debug("✅ Admin retrieved review detail for ID: {}", reviewId);

            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("❌ Admin error getting review detail {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get review detail"));
        }
    }

    /**
     * ADMIN: Thống kê review
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReviewStats() {
        log.debug("📊 Admin getting review stats");

        try {
            // TODO: Thêm method trong service để lấy stats thực tế
            // Map<String, Object> stats = reviewService.getReviewStats();

            // Tạm thời trả về mock data
            Map<String, Object> stats = Map.of(
                    "totalReviews", 0,
                    "activeReviews", 0,
                    "reportedReviews", 0,
                    "hiddenReviews", 0,
                    "courseReviews", 0,
                    "mockTestReviews", 0,
                    "averageRating", 0.0
            );

            log.debug("✅ Admin retrieved review stats");
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Admin error getting review stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get review stats"));
        }
    }

    // ====================================================================
    // HELPER METHODS
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