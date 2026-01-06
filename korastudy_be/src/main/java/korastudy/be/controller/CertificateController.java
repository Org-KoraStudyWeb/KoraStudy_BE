package korastudy.be.controller;

import korastudy.be.dto.response.CertificateDTO;
import korastudy.be.dto.response.CertificateDetailDTO;
import korastudy.be.dto.response.course.CourseProgressDetailDTO;
import korastudy.be.entity.Certificate;
import korastudy.be.entity.User.User;
import korastudy.be.repository.CertificateRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.impl.CourseCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CourseCompletionService courseCompletionService;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByAccount_Username(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCertificate(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        if (!courseCompletionService.isEligibleForCertificate(userId, courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn chưa hoàn thành khóa học này");
        }

        try {
            Certificate certificate = courseCompletionService.createCertificateIfEligible(userId, courseId);
            CertificateDTO certificateDTO = courseCompletionService.convertToDTO(certificate);
            return ResponseEntity.ok(certificateDTO);
        } catch (Exception e) {
            Optional<Certificate> certificateOpt = courseCompletionService.getUserCertificate(userId, courseId);
            if (certificateOpt.isPresent()) {
                CertificateDTO certificateDTO = courseCompletionService.convertToDTO(certificateOpt.get());
                return ResponseEntity.ok(certificateDTO);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể lấy certificate: " + e.getMessage());
        }
    }

    @GetMapping("/my-certificates")
    public ResponseEntity<?> getMyCertificates(@AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        List<CertificateDTO> certificates = courseCompletionService.getUserCertificateDTOs(userId);

        if (certificates.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bạn chưa có certificate nào");
            response.put("totalCertificates", 0);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("certificates", certificates);
        response.put("totalCertificates", certificates.size());
        response.put("stats", courseCompletionService.getCertificateStats(userId));

        return ResponseEntity.ok(response);
    }

    /**
     *  FIX: Thêm try-catch để handle duplicate error
     */
    @GetMapping("/courses/{courseId}/has-certificate")
    public ResponseEntity<Map<String, Object>> hasCertificate(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("courseId", courseId);

        try {
            boolean hasCertificate = courseCompletionService.hasCertificate(userId, courseId);
            response.put("hasCertificate", hasCertificate);
            response.put("isEligibleForCertificate", courseCompletionService.isEligibleForCertificate(userId, courseId));

            if (hasCertificate) {
                Optional<Certificate> certOpt = courseCompletionService.getUserCertificate(userId, courseId);
                certOpt.ifPresent(certificate -> {
                    response.put("certificateId", certificate.getId());
                    response.put("certificateCode", certificate.getCertificateCode());
                    response.put("grade", certificate.getGrade());
                    response.put("averageScore", certificate.getAverageScore());
                    response.put("certificateDate", certificate.getCertificateDate());
                });
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Error checking certificate for user {} course {}: {}", userId, courseId, e.getMessage());
            response.put("hasCertificate", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/courses/{courseId}/average-score")
    public ResponseEntity<Map<String, Object>> getAverageScore(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);
        Double averageScore = courseCompletionService.getUserAverageScore(userId, courseId);
        boolean isCompleted = courseCompletionService.isCourseCompleted(userId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("averageScore", averageScore);
        response.put("userId", userId);
        response.put("courseId", courseId);
        response.put("isCourseCompleted", isCompleted);
        response.put("isEligibleForCertificate", courseCompletionService.isEligibleForCertificate(userId, courseId));
        response.put("hasCertificate", courseCompletionService.hasCertificate(userId, courseId));

        if (averageScore != null) {
            korastudy.be.entity.Enum.CertificateGrade grade = korastudy.be.entity.Enum.CertificateGrade.fromScore(averageScore);
            response.put("grade", grade.name());
            response.put("gradeDisplayName", getGradeDisplayName(grade));
        } else {
            response.put("grade", "N/A");
            response.put("gradeDisplayName", "Chưa có điểm");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/{courseId}/with-details")
    public ResponseEntity<?> getCertificateWithDetails(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        CourseProgressDetailDTO progressDetail = courseCompletionService.getCourseProgressDetail(userId, courseId);

        Optional<Certificate> certificateOpt = courseCompletionService.getUserCertificate(userId, courseId);

        CertificateDTO certificateDTO = null;
        if (certificateOpt.isPresent()) {
            certificateDTO = courseCompletionService.convertToDTO(certificateOpt.get());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("progress", progressDetail);
        response.put("certificate", certificateDTO);
        response.put("isEligible", courseCompletionService.isEligibleForCertificate(userId, courseId));
        response.put("isCourseCompleted", courseCompletionService.isCourseCompleted(userId, courseId));
        response.put("hasCertificate", courseCompletionService.hasCertificate(userId, courseId));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint quan trọng nhất - claim certificate
     */
    @PostMapping("/courses/{courseId}/claim")
    public ResponseEntity<?> claimCertificate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);
        String lockKey = "cert_claim_" + userId + "_" + courseId;

        synchronized (lockKey.intern()) {  // 🔒 Lock theo user + course
            log.info("🎓 User {} claiming certificate for course {}", userId, courseId);

            try {
                // Check lại trong lock
                if (courseCompletionService.hasCertificate(userId, courseId)) {
                    Optional<Certificate> existingCert =
                            courseCompletionService.getUserCertificate(userId, courseId);

                    if (existingCert.isPresent()) {
                        CertificateDTO certificateDTO =
                                courseCompletionService.convertToDTO(existingCert.get());

                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Bạn đã có certificate rồi");
                        response.put("certificate", certificateDTO);
                        response.put("alreadyExists", true);

                        return ResponseEntity.ok(response);
                    }
                }

                if (!courseCompletionService.isEligibleForCertificate(userId, courseId)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Bạn chưa đủ điều kiện nhận certificate");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }

                // Tạo certificate
                Certificate certificate =
                        courseCompletionService.createCertificateIfEligible(userId, courseId);
                CertificateDTO certificateDTO =
                        courseCompletionService.convertToDTO(certificate);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Certificate đã được tạo thành công");
                response.put("certificate", certificateDTO);
                response.put("alreadyExists", false);

                return ResponseEntity.ok(response);

            } catch (DataIntegrityViolationException e) {
                // Duplicate key - fetch existing
                log.warn(" Duplicate detected, fetching existing certificate");
                Optional<Certificate> existingCert =
                        courseCompletionService.getUserCertificate(userId, courseId);

                if (existingCert.isPresent()) {
                    CertificateDTO certificateDTO =
                            courseCompletionService.convertToDTO(existingCert.get());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("certificate", certificateDTO);
                    response.put("alreadyExists", true);

                    return ResponseEntity.ok(response);
                }

                throw new RuntimeException("Failed to create or fetch certificate");

            } catch (Exception e) {
                log.error(" Error: {}", e.getMessage(), e);

                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không thể tạo certificate: " + e.getMessage());

                return ResponseEntity.badRequest().body(response);
            }
        }
    }

    @PostMapping("/courses/{courseId}/force-generate")
    public ResponseEntity<?> forceGenerateCertificate(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        try {
            if (!courseCompletionService.isCourseCompleted(userId, courseId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Bạn chưa hoàn thành khóa học");
                errorResponse.put("isCourseCompleted", false);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (courseCompletionService.hasCertificate(userId, courseId)) {
                Optional<Certificate> existingCert = courseCompletionService.getUserCertificate(userId, courseId);
                CertificateDTO certificateDTO = existingCert.map(courseCompletionService::convertToDTO).orElse(null);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Certificate đã tồn tại");
                response.put("certificate", certificateDTO);
                response.put("alreadyExists", true);

                return ResponseEntity.ok(response);
            }

            Certificate certificate = courseCompletionService.createCertificateIfEligible(userId, courseId);
            CertificateDTO certificateDTO = courseCompletionService.convertToDTO(certificate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Certificate đã được tạo thành công");
            response.put("certificate", certificateDTO);
            response.put("alreadyExists", false);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tạo certificate: " + e.getMessage());
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     *  API lấy chi tiết certificate - BẮT BUỘC đăng nhập
     * Chỉ user sở hữu hoặc admin mới được xem
     */
    @GetMapping("/{certificateId}")
    public ResponseEntity<?> getCertificateDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long certificateId) {

        Long userId = getUserId(userDetails);

        try {
            // 1. Tìm certificate
            Optional<Certificate> certificateOpt = courseCompletionService.getCertificateById(certificateId);

            if (certificateOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Certificate không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Certificate certificate = certificateOpt.get();

            // 2. Kiểm tra quyền truy cập CHẶT CHẼ
            boolean isOwner = certificate.getUser().getId().equals(userId);
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isOwner && !isAdmin) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Bạn không có quyền xem certificate này");
                response.put("code", "ACCESS_DENIED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 3. Lấy chi tiết ĐẦY ĐỦ (chỉ khi có quyền)
            CertificateDetailDTO certificateDetail = courseCompletionService.getCertificateDetail(certificateId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificate", certificateDetail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Error getting certificate by id {}: {}", certificateId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin certificate");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy certificate bằng certificateCode (dùng cho chia sẻ)
     * Chỉ user sở hữu hoặc admin mới được xem
     */
    @GetMapping("/code/{certificateCode}")
    public ResponseEntity<?> getCertificateByCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String certificateCode) {

        Long userId = getUserId(userDetails);

        try {
            // 1. Tìm certificate bằng code
            Optional<Certificate> certificateOpt = courseCompletionService.getCertificateByCode(certificateCode);

            if (certificateOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Certificate không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Certificate certificate = certificateOpt.get();

            // 2. Kiểm tra quyền
            boolean isOwner = certificate.getUser().getId().equals(userId);
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isOwner && !isAdmin) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Bạn không có quyền xem certificate này");
                response.put("code", "ACCESS_DENIED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 3. Lấy chi tiết
            CertificateDetailDTO certificateDetail = courseCompletionService.getCertificateDetail(certificate.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificate", certificateDetail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Error getting certificate by code {}: {}", certificateCode, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin certificate");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xác thực công khai bằng certificateCode
     * KHÔNG cần đăng nhập
     */
    @GetMapping("/public/verify/{certificateCode}")
    public ResponseEntity<?> verifyCertificatePublic(@PathVariable String certificateCode) {

        try {
            Optional<Certificate> certificateOpt = courseCompletionService.getCertificateByCode(certificateCode);

            if (certificateOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Certificate không tồn tại hoặc đã bị thu hồi");
                response.put("certificateCode", certificateCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Certificate certificate = certificateOpt.get();

            // Chỉ trả về thông tin công khai, không có thông tin nhạy cảm
            Map<String, Object> verificationInfo = new HashMap<>();
            verificationInfo.put("valid", true);
            verificationInfo.put("certificateCode", certificate.getCertificateCode());
            verificationInfo.put("certificateName", certificate.getCertificateName());
            verificationInfo.put("issueDate", certificate.getCertificateDate());
            verificationInfo.put("grade", certificate.getGrade());

            // Mask user name (chỉ hiển thị họ và tên viết tắt)
            String fullName = certificate.getUser().getFullName();
            verificationInfo.put("userName", fullName);

            verificationInfo.put("courseName", certificate.getCourse().getCourseName());
            verificationInfo.put("issuedBy", "Kora Study");
            verificationInfo.put("verificationDate", java.time.LocalDate.now());

            return ResponseEntity.ok(verificationInfo);

        } catch (Exception e) {
            log.error(" Error verifying certificate {}: {}", certificateCode, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Lỗi khi xác thực certificate");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ADMIN: Lấy certificate của user bất kỳ
     * API: GET /api/v1/certificates/admin/users/{userId}/courses/{courseId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}/courses/{courseId}")
    public ResponseEntity<?> getCertificateByUserAndCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {

        try {
            Optional<Certificate> certificateOpt = courseCompletionService.getUserCertificate(userId, courseId);

            if (certificateOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Certificate không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Certificate certificate = certificateOpt.get();
            CertificateDetailDTO certificateDetail = courseCompletionService.getCertificateDetail(certificate.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificate", certificateDetail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Admin error getting certificate for user {} course {}: {}", userId, courseId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin certificate");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ADMIN: Xóa certificate của user bất kỳ
     * API: DELETE /api/v1/certificates/admin/{certificateId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{certificateId}")
    public ResponseEntity<?> deleteCertificateAsAdmin(@PathVariable Long certificateId) {

        try {
            Optional<Certificate> certificateOpt = courseCompletionService.getCertificateById(certificateId);

            if (certificateOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Certificate không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            certificateRepository.deleteById(certificateId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Certificate đã được xóa thành công");
            response.put("certificateId", certificateId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Admin error deleting certificate {}: {}", certificateId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi xóa certificate");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCertificate(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long courseId) {

        Long userId = getUserId(userDetails);

        try {
            Optional<Certificate> certificateOpt = courseCompletionService.getUserCertificate(userId, courseId);
            if (certificateOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Certificate đã được xóa");
                response.put("certificateId", certificateOpt.get().getId());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Certificate không tồn tại");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa certificate: " + e.getMessage());
        }
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
}