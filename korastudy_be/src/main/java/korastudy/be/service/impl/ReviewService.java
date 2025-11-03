package korastudy.be.service.impl;

import korastudy.be.dto.request.course.ReviewRequest;
import korastudy.be.dto.response.course.ReviewDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Review;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.ReviewRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IEnrollmentService;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final IEnrollmentService enrollmentService;

    @Override
    public ReviewDTO addReview(Long userId, ReviewRequest request) {
        // Kiểm tra xem user đã đăng ký khóa học chưa
        if (!enrollmentService.isUserEnrolledInCourse(userId, request.getCourseId())) {
            throw new IllegalStateException("Bạn cần đăng ký khóa học trước khi đánh giá");
        }

        // Kiểm tra xem đã đánh giá trước đó chưa
        reviewRepository.findByUserIdAndCourseId(userId, request.getCourseId())
                .ifPresent(r -> {
                    throw new IllegalStateException("Bạn đã đánh giá khóa học này rồi");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        Review review = Review.builder()
                .user(user)
                .course(course)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToDTO(savedReview);
    }

    @Override
    public ReviewDTO updateReview(Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        review.setCourse(course);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        return mapToDTO(updatedReview);
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
        return mapToDTO(review);
    }

    @Override
    public List<ReviewDTO> getCourseReviews(Long courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageCourseRating(Long courseId) {
        Double avgRating = reviewRepository.findAverageRatingByCourseId(courseId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    public ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .userAvatar(review.getUser().getAvatar())
                .courseId(review.getCourse().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
    
    @Override
    public List<ReviewDTO> getCourseReviewsWithPagination(Long courseId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByCourseId(courseId, pageable);
        return reviewPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countReviewsByCourseId(Long courseId) {
        return reviewRepository.countByCourseId(courseId);
    }
}
