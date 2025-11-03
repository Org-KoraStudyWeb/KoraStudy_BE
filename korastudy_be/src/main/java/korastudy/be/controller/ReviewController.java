package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.ReviewRequest;
import korastudy.be.dto.response.course.ReviewDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ReviewDTO> addReview(@Valid @RequestBody ReviewRequest request, Principal principal) {
        // Từ principal, lấy userId hiện tại
        // Giả định rằng principal.getName() sẽ trả về username, và bạn cần xác định userId từ đó
        // Đây chỉ là một giả định, bạn cần điều chỉnh theo cách xác thực của bạn
        Long userId = 1L; // Thay thế bằng cách lấy userId thực từ Principal
        
        ReviewDTO reviewDTO = reviewService.addReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        ReviewDTO review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewDTO>> getCourseReviews(@PathVariable Long courseId) {
        List<ReviewDTO> reviews = reviewService.getCourseReviews(courseId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/course/{courseId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long courseId) {
        double avgRating = reviewService.getAverageCourseRating(courseId);
        return ResponseEntity.ok(avgRating);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        ReviewDTO reviewDTO = reviewService.updateReview(id, request);
        return ResponseEntity.ok(reviewDTO);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiSuccess> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiSuccess.of("Xóa đánh giá thành công"));
    }
}
