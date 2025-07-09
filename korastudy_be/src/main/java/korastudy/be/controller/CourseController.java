package korastudy.be.controller;

import korastudy.be.dto.request.course.CreateCourseRequest;
import korastudy.be.dto.response.course.CourseResponse;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.impl.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 1. Tìm kiếm + phân trang
    @GetMapping("/search")
    public ResponseEntity<Page<CourseResponse>> searchCourses(@RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(courseService.searchCourses(keyword, pageable));
    }

    // 2. Phân trang toàn bộ khóa học
    @GetMapping("/page")
    public ResponseEntity<Page<CourseResponse>> getAllCoursesPaged(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(courseService.getAllCoursesPaged(pageable));
    }

    // 3. Lấy tất cả khóa học (full - không phân trang, chỉ admin dùng)
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // 4. Tạo khóa học mới
    @PostMapping
    public ResponseEntity<ApiSuccess> createCourse(@RequestBody CreateCourseRequest dto) {
        courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of("Khóa học đã được tạo thành công"));
    }

    // 5. Cập nhật khóa học
    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccess> updateCourse(@PathVariable Long id, @RequestBody CreateCourseRequest dto) {
        courseService.updateCourse(id, dto);
        return ResponseEntity.ok(ApiSuccess.of("Khóa học đã được cập nhật"));
    }

    // 6. Lấy các khóa học đã publish
    @GetMapping("/published")
    public ResponseEntity<List<CourseResponse>> getPublishedCourses() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    // 7. Lấy chi tiết 1 khóa học
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // 8. Xóa khóa học
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccess> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiSuccess.of("Khóa học đã được xóa"));
    }
}

