package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.CourseCreateRequest;
import korastudy.be.dto.request.course.CourseUpdateRequest;
import korastudy.be.dto.response.course.CourseDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllPublishedCourses() {
        List<CourseDTO> courses = courseService.getAllCourses(true); // Chỉ lấy các khóa học đã được xuất bản
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        
        // Tăng số lượt xem
        courseService.incrementViewCount(id);
        
        return ResponseEntity.ok(course);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        CourseDTO courseDTO = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(courseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateRequest request) {
        CourseDTO courseDTO = courseService.updateCourse(id, request);
        return ResponseEntity.ok(courseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiSuccess> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiSuccess.of("Xóa khóa học thành công"));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<CourseDTO> publishCourse(@PathVariable Long id, @RequestParam boolean isPublished) {
        CourseDTO courseDTO = courseService.publishCourse(id, isPublished);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String keyword) {
        List<CourseDTO> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<CourseDTO>> getAllCoursesForAdmin() {
        List<CourseDTO> courses = courseService.getAllCourses(false); // Lấy tất cả khóa học (cả published và chưa published)
        return ResponseEntity.ok(courses);
    }
}
