package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.CourseCreateRequest;
import korastudy.be.dto.request.course.CourseUpdateRequest;
import korastudy.be.dto.response.course.CourseDTO;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.dto.response.course.ReviewDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.payload.response.PagedResponse;
import korastudy.be.service.ICourseService;
import korastudy.be.service.IEnrollmentService;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminCourseController {

    private final ICourseService courseService;
    private final IEnrollmentService enrollmentService;
    private final IReviewService reviewService;

    @GetMapping
    public ResponseEntity<PagedResponse<CourseDTO>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        List<CourseDTO> courses = courseService.getAllCoursesWithPagination(pageable);
        long totalElements = courseService.countCourses();
        
        return ResponseEntity.ok(new PagedResponse<>(courses, page, size, totalElements, (int) Math.ceil((double) totalElements / size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        CourseDTO createdCourse = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateRequest request) {
        CourseDTO updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccess> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiSuccess.of("Xóa khóa học thành công"));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<CourseDTO> togglePublishStatus(@PathVariable Long id, @RequestParam boolean isPublished) {
        CourseDTO updatedCourse = courseService.publishCourse(id, isPublished);
        return ResponseEntity.ok(updatedCourse);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", courseService.countCourses());
        stats.put("publishedCourses", courseService.countPublishedCourses());
        stats.put("unpublishedCourses", courseService.countUnpublishedCourses());
        stats.put("totalEnrollments", enrollmentService.countTotalEnrollments());
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<PagedResponse<EnrollmentDTO>> getCourseEnrollments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        
        List<EnrollmentDTO> enrollments = enrollmentService.getCourseEnrollmentsWithPagination(id, pageable);
        long totalElements = enrollmentService.countEnrollmentsByCourseId(id);
        
        return ResponseEntity.ok(new PagedResponse<>(enrollments, page, size, totalElements, (int) Math.ceil((double) totalElements / size)));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PagedResponse<ReviewDTO>> getCourseReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        
        List<ReviewDTO> reviews = reviewService.getCourseReviewsWithPagination(id, pageable);
        long totalElements = reviewService.countReviewsByCourseId(id);
        
        return ResponseEntity.ok(new PagedResponse<>(reviews, page, size, totalElements, (int) Math.ceil((double) totalElements / size)));
    }

    @DeleteMapping("/{courseId}/reviews/{reviewId}")
    public ResponseEntity<ApiSuccess> deleteReview(@PathVariable Long courseId, @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiSuccess.of("Xóa đánh giá thành công"));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<CourseDTO>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        
        List<CourseDTO> courses = courseService.searchCoursesWithPagination(keyword, pageable);
        long totalElements = courseService.countSearchResults(keyword);
        
        return ResponseEntity.ok(new PagedResponse<>(courses, page, size, totalElements, (int) Math.ceil((double) totalElements / size)));
    }
}
