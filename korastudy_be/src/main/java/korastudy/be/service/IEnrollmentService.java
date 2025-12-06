package korastudy.be.service;

import korastudy.be.dto.request.enrollment.EnrollmentRequest;
import korastudy.be.dto.response.enrollment.EnrollmentDTO;
import korastudy.be.dto.response.enrollment.EnrollmentDetailDTO;
import korastudy.be.dto.response.enrollment.EnrollmentStatsDTO;
import korastudy.be.entity.Enum.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEnrollmentService {

    // === CORE FUNCTIONALITY ===

    /**
     * Đăng ký khóa học cho user (chỉ free course)
     */
    EnrollmentDTO enrollUserToCourse(EnrollmentRequest request, String username);

    // === USER FUNCTIONALITY ===

    /**
     * Lấy danh sách khóa học user đã đăng ký (cho trang "Khóa học của tôi")
     */
    List<EnrollmentDTO> getUserEnrollments(Long userId);

    /**
     * Lọc khóa học theo trạng thái: ACTIVE (Đang học), COMPLETED (Hoàn thành)
     */
    List<EnrollmentDTO> getUserEnrollmentsByStatus(Long userId, EnrollmentStatus status);

    /**
     * Kiểm tra user đã đăng ký khóa học chưa (cho nút "Đăng ký"/"Tiếp tục học")
     */
    boolean isUserEnrolledInCourse(Long userId, Long courseId);

    // === ADMIN FUNCTIONALITY ===

    /**
     * Lấy danh sách user đã đăng ký khóa học (cho admin quản lý) - CÓ PHÂN TRANG
     */
    Page<EnrollmentDetailDTO> getCourseEnrollments(Long courseId, Pageable pageable);

    /**
     * Đếm số lượng đăng ký của khóa học (cho thống kê)
     */
    long countEnrollmentsByCourseId(Long courseId);

    // === PROGRESS MANAGEMENT ===

    /**
     * Cập nhật tiến độ học tập thủ công (0-100%)
     */
    EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, double progress);

    /**
     * TỰ ĐỘNG cập nhật tiến độ từ bài học (gọi khi user hoàn thành bài học)
     */
    void updateProgressFromLessons(Long userId, Long courseId, int completedLessons);

    // === ENROLLMENT MANAGEMENT ===

    /**
     * Hủy đăng ký khóa học
     */
    void cancelEnrollment(Long enrollmentId);

    /**
     * Lấy thông tin chi tiết enrollment theo ID
     */
    EnrollmentDTO getEnrollmentById(Long enrollmentId);

    // === DASHBOARD STATS ===

    /**
     * Lấy thống kê tổng quan cho dashboard
     * - Tổng số enrollment
     * - Số enrollment active
     * - Số enrollment completed
     */
    EnrollmentStatsDTO getEnrollmentStats();
}