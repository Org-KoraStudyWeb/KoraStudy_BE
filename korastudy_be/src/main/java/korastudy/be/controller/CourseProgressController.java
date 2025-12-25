package korastudy.be.controller;

import korastudy.be.dto.response.course.CourseProgressDTO;
import korastudy.be.dto.response.course.CourseProgressDetailDTO;
import korastudy.be.entity.User.User;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.impl.CourseCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courseProgress/")
@RequiredArgsConstructor
public class CourseProgressController {

    private final CourseCompletionService courseCompletionService;
    private final UserRepository userRepository;

    // Helper method để lấy userId từ UserDetails
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByAccount_Username(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    /**
     * Lấy tiến độ khóa học (tổng quan)
     */
    @GetMapping("/{courseId}/progress")
    public ResponseEntity<CourseProgressDTO> getCourseProgress(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);
        CourseProgressDTO progress = courseCompletionService.getCourseProgressSummary(userId, courseId);

        return ResponseEntity.ok(progress);
    }

    /**
     * Lấy tiến độ chi tiết với đánh giá và gợi ý
     */
    @GetMapping("/{courseId}/progress-detail")
    public ResponseEntity<CourseProgressDetailDTO> getCourseProgressDetail(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);
        CourseProgressDetailDTO progressDetail = courseCompletionService.getCourseProgressDetail(userId, courseId);

        return ResponseEntity.ok(progressDetail);
    }

    /**
     * Kiểm tra điều kiện nhận certificate
     */
    @GetMapping("/{courseId}/certificate/eligibility")
    public ResponseEntity<?> checkCertificateEligibility(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);
        boolean isEligible = courseCompletionService.isEligibleForCertificate(userId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("isEligible", isEligible);
        response.put("isCourseCompleted", courseCompletionService.isCourseCompleted(userId, courseId));
        response.put("hasCertificate", courseCompletionService.hasCertificate(userId, courseId));
        response.put("message", isEligible ? "Bạn đủ điều kiện nhận chứng chỉ" : "Bạn chưa đủ điều kiện nhận chứng chỉ");

        // Thêm thông tin chi tiết nếu chưa đủ điều kiện
        if (!isEligible) {
            CourseProgressDetailDTO progressDetail = courseCompletionService.getCourseProgressDetail(userId, courseId);
            response.put("requirements", progressDetail.getRecommendations());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật tiến độ (gọi khi hoàn thành lesson hoặc quiz)
     */
    @PostMapping("/{courseId}/update-progress")
    public ResponseEntity<?> updateCourseProgress(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        try {
            courseCompletionService.updateCourseProgress(userId, courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã cập nhật tiến độ thành công");

            // Trả về thông tin tiến độ mới
            CourseProgressDTO progress = courseCompletionService.getCourseProgressSummary(userId, courseId);
            response.put("progress", progress);

            // Kiểm tra xem có certificate mới không
            boolean hasCertificate = courseCompletionService.hasCertificate(userId, courseId);
            response.put("hasCertificate", hasCertificate);
            response.put("isCourseCompleted", courseCompletionService.isCourseCompleted(userId, courseId));
            response.put("isEligibleForCertificate", courseCompletionService.isEligibleForCertificate(userId, courseId));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể cập nhật tiến độ");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Kiểm tra trạng thái hoàn thành khóa học
     */
    @GetMapping("/{courseId}/completion-status")
    public ResponseEntity<?> getCompletionStatus(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        boolean isCompleted = courseCompletionService.isCourseCompleted(userId, courseId);
        boolean hasCertificate = courseCompletionService.hasCertificate(userId, courseId);
        boolean isEligible = courseCompletionService.isEligibleForCertificate(userId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("isCourseCompleted", isCompleted);
        response.put("hasCertificate", hasCertificate);
        response.put("isEligibleForCertificate", isEligible);
        response.put("progressPercentage", courseCompletionService.getCourseProgressSummary(userId, courseId).getProgressPercentage());

        // Tự động tạo certificate nếu đủ điều kiện nhưng chưa có
        if (isEligible && !hasCertificate) {
            response.put("certificateAutoCreated", true);
            response.put("message", "Certificate sẽ được tạo tự động khi bạn hoàn thành bài học/quiz cuối cùng");
        }

        // Thêm thông tin tiến độ nếu chưa hoàn thành
        if (!isCompleted) {
            CourseProgressDTO progress = courseCompletionService.getCourseProgressSummary(userId, courseId);
            response.put("currentProgress", progress);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin động viên và gợi ý dựa trên tiến độ
     */
    @GetMapping("/{courseId}/motivation")
    public ResponseEntity<?> getMotivation(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        CourseProgressDetailDTO progressDetail = courseCompletionService.getCourseProgressDetail(userId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("motivationalMessage", progressDetail.getMotivationalMessage());
        response.put("recommendations", progressDetail.getRecommendations());
        response.put("lessonCompletionRate", progressDetail.getLessonCompletionRate());
        response.put("quizPassRate", progressDetail.getQuizPassRate());
        response.put("averageQuizScore", progressDetail.getAverageQuizScore());
        response.put("isCompleted", progressDetail.isCompleted());
        response.put("hasCertificate", progressDetail.isHasCertificate());

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin certificate grade (nếu có)
     */
    @GetMapping("/{courseId}/certificate-grade")
    public ResponseEntity<?> getCertificateGrade(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        // Lấy điểm trung bình
        Double averageScore = courseCompletionService.getUserAverageScore(userId, courseId);
        boolean hasCertificate = courseCompletionService.hasCertificate(userId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("averageScore", averageScore);
        response.put("hasCertificate", hasCertificate);
        response.put("isCourseCompleted", courseCompletionService.isCourseCompleted(userId, courseId));

        if (averageScore != null) {
            korastudy.be.entity.Enum.CertificateGrade grade = korastudy.be.entity.Enum.CertificateGrade.fromScore(averageScore);
            response.put("grade", grade.name());
            response.put("gradeDisplayName", getGradeDisplayName(grade));
            response.put("message", getGradeMessage(grade));
        }

        if (hasCertificate) {
            var certOpt = courseCompletionService.getUserCertificate(userId, courseId);
            certOpt.ifPresent(certificate -> {
                response.put("certificateId", certificate.getId());
                response.put("certificateCode", certificate.getCertificateCode());
                response.put("certificateDate", certificate.getCertificateDate());
            });
        }

        return ResponseEntity.ok(response);
    }

    private String getGradeDisplayName(korastudy.be.entity.Enum.CertificateGrade grade) {
        return switch (grade) {
            case EXCELLENT -> "Xuất sắc";
            case GOOD -> "Giỏi";
            case FAIR -> "Khá";
            case PASS -> "Hoàn thành";
            default -> "Hoàn thành";
        };
    }

    private String getGradeMessage(korastudy.be.entity.Enum.CertificateGrade grade) {
        return switch (grade) {
            case EXCELLENT -> "Bạn đạt thành tích xuất sắc!";
            case GOOD -> "Bạn có kết quả rất tốt!";
            case FAIR -> "Bạn đã hoàn thành tốt khóa học!";
            case PASS -> "Chúc mừng bạn đã hoàn thành khóa học!";
            default -> "Chúc mừng bạn!";
        };
    }
}