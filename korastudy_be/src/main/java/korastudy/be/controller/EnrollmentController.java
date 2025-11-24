package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.EnrollmentRequest;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.ICourseService;
import korastudy.be.service.IEnrollmentService;
import korastudy.be.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final IEnrollmentService enrollmentService;
    private final IUserService userService;
    private final UserRepository userRepository;

    /*
     * Đăng ký khóa học free thôi còn nếu đăng ký khóa học có phí thì sẽ qua payment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'CONTENT_MANAGER')")
    public ResponseEntity<EnrollmentDTO> enrollCourse(@Valid @RequestBody EnrollmentRequest request, Principal principal // ✅ ĐÃ THÊM Principal
    ) {
        String username = principal.getName();
        EnrollmentDTO enrollmentDTO = enrollmentService.enrollUserToCourse(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getUserEnrollments(@PathVariable Long userId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollments(userId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Long courseId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getCourseEnrollments(courseId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments(Principal principal) {
        String username = principal.getName();

        // ✅ Dùng trực tiếp repository
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollments(user.getId());
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

        // ✅ Dùng trực tiếp repository thay vì userService
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        boolean isEnrolled = enrollmentService.isUserEnrolledInCourse(user.getId(), courseId);
        return ResponseEntity.ok(isEnrolled);
    }
}