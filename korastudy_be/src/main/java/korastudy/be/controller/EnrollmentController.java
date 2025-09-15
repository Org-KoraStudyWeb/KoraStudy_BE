package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.EnrollmentRequest;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IEnrollmentService;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'DELIVERY_MANAGER', 'CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<EnrollmentDTO> enrollCourse(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentDTO enrollmentDTO = enrollmentService.enrollUserToCourse(request);
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
        // Từ principal, lấy userId hiện tại
        // Giả định rằng principal.getName() sẽ trả về username, và bạn cần xác định userId từ đó
        // Đây chỉ là một giả định, bạn cần điều chỉnh theo cách xác thực của bạn
        Long userId = 1L; // Thay thế bằng cách lấy userId thực từ Principal
        
        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollments(userId);
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
}
