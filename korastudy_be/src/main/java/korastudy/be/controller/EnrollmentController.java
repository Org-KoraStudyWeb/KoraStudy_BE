package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.enrollment.EnrollmentRequest;
import korastudy.be.dto.response.enrollment.EnrollmentDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Enum.EnrollmentStatus;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final IEnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    /*
     * Đăng ký khóa học free thôi còn nếu đăng ký khóa học có phí thì sẽ qua payment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'CONTENT_MANAGER')")
    public ResponseEntity<EnrollmentDTO> enrollCourse(@Valid @RequestBody EnrollmentRequest request, Principal principal) {
        String username = principal.getName();
        EnrollmentDTO enrollmentDTO = enrollmentService.enrollUserToCourse(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentDTO);
    }

    // Trong CourseController
    @GetMapping("/{courseId}/check-free")
    public ResponseEntity<Map<String, Boolean>> checkCourseIsFree(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean isFree = course.getCoursePrice() <= 0 || course.isFree();

        return ResponseEntity.ok(Collections.singletonMap("isFree", isFree));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    // Cho admin xem enrollments của user (có thể thêm phân trang sau)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getUserEnrollments(@PathVariable Long userId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollments(userId);
        return ResponseEntity.ok(enrollments);
    }


    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollments(user.getId());
        return ResponseEntity.ok(enrollments);
    }

    // Lọc khóa học của tôi theo trạng thái
    @GetMapping("/my-courses/filter")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollmentsByStatus(Principal principal, @RequestParam EnrollmentStatus status) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollmentsByStatus(user.getId(), status);
        return ResponseEntity.ok(enrollments);
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<EnrollmentDTO> updateProgress(@PathVariable Long id, @RequestParam double progress) {
        EnrollmentDTO enrollment = enrollmentService.updateEnrollmentProgress(id, progress);
        return ResponseEntity.ok(enrollment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiSuccess> cancelEnrollment(@PathVariable Long id) {
        enrollmentService.cancelEnrollment(id);
        return ResponseEntity.ok(ApiSuccess.of("Hủy đăng ký khóa học thành công"));
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Boolean> checkEnrollment(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentService.isUserEnrolledInCourse(userId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    @GetMapping("/check-my-enrollment")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Boolean> checkMyEnrollment(@RequestParam Long courseId, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        boolean isEnrolled = enrollmentService.isUserEnrolledInCourse(user.getId(), courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    // API cho dashboard stats
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<?> getEnrollmentStats() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentStats());
    }
}