package korastudy.be.service;

import korastudy.be.dto.request.course.ReviewRequest;
import korastudy.be.dto.response.course.ReviewDTO;
import korastudy.be.entity.Course.Review;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IReviewService {
    
    ReviewDTO addReview(Long userId, ReviewRequest request);
    
    ReviewDTO updateReview(Long reviewId, ReviewRequest request);
    
    void deleteReview(Long reviewId);
    
    ReviewDTO getReviewById(Long reviewId);
    
    List<ReviewDTO> getCourseReviews(Long courseId);
    
    double getAverageCourseRating(Long courseId);
    
    ReviewDTO mapToDTO(Review review);
    
    // Methods for admin panel with pagination
    List<ReviewDTO> getCourseReviewsWithPagination(Long courseId, Pageable pageable);
    
    long countReviewsByCourseId(Long courseId);
}
