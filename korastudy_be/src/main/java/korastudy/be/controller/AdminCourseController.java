package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.CourseCreateRequest;
import korastudy.be.dto.request.course.CourseUpdateRequest;
import korastudy.be.dto.response.course.CourseDTO;
import korastudy.be.dto.response.review.ReviewDTO;
import korastudy.be.dto.response.enrollment.EnrollmentDetailDTO;
import korastudy.be.dto.response.enrollment.EnrollmentStatsDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.payload.response.PagedResponse;
import korastudy.be.service.ICourseService;
import korastudy.be.service.IEnrollmentService;
import korastudy.be.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<PagedResponse<CourseDTO>> getAllCourses(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "desc") String sortDir, @RequestParam(required = false) String keyword // <— thêm dòng này
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        List<CourseDTO> courses;
        long totalElements;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            courses = courseService.searchCoursesWithPagination(kw, pageable);
            totalElements = courseService.countSearchResults(kw);
        } else {
            courses = courseService.getAllCoursesWithPagination(pageable);
            totalElements = courseService.countCourses();
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);
        return ResponseEntity.ok(new PagedResponse<>(courses, page, size, totalElements, totalPages));
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

        EnrollmentStatsDTO enrollmentStats = enrollmentService.getEnrollmentStats();
        stats.put("totalEnrollments", enrollmentStats.getTotalEnrollments());
        stats.put("activeEnrollments", enrollmentStats.getActiveEnrollments());
        stats.put("completedEnrollments", enrollmentStats.getCompletedEnrollments());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<PagedResponse<EnrollmentDetailDTO>> getCourseEnrollments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<EnrollmentDetailDTO> enrollmentPage = enrollmentService.getCourseEnrollments(id, pageable);

        return ResponseEntity.ok(new PagedResponse<>(
                enrollmentPage.getContent(),
                page,
                size,
                enrollmentPage.getTotalElements(),
                enrollmentPage.getTotalPages()
        ));
    }

}
