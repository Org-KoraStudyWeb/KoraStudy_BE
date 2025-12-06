package korastudy.be.mapper;

import korastudy.be.dto.request.enrollment.EnrollmentRequest;
import korastudy.be.dto.response.enrollment.EnrollmentDTO;
import korastudy.be.dto.response.enrollment.EnrollmentDetailDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.Enum.EnrollmentStatus;
import korastudy.be.entity.User.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EnrollmentMapper {

    // ==================== TO ENTITY (KHÔNG ĐỔI) ====================
    public static Enrollment toEntity(EnrollmentRequest request, User user, Course course) {
        return Enrollment.builder()
                .user(user)
                .course(course)
                .enrollDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(6))
                .progress(0.0)
                .status(EnrollmentStatus.ACTIVE)
                .completedLessons(0)
                .lastAccessed(LocalDateTime.now())
                .build();
    }

    // ==================== TO DTO (CHO USER) ====================
    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        Course course = enrollment.getCourse();

        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())           // Chỉ giữ ID
                .courseId(course.getId())
                .courseName(course.getCourseName())
                .courseDescription(course.getCourseDescription())
                .courseThumbnail(course.getCourseImageUrl())
                .level(course.getCourseLevel())
                .enrollDate(enrollment.getEnrollDate())
                .expiryDate(enrollment.getExpiryDate())
                .lastAccessed(enrollment.getLastAccessed())
                .progress(enrollment.getProgress())
                .status(enrollment.getStatus().name())
                .completedLessons(enrollment.getCompletedLessons())
                .totalLessons(calculateTotalLessons(course))    // Tính từ sections
                .isExpired(enrollment.isExpired())
                .build();
    }

    // ==================== TO DETAIL DTO (CHO ADMIN) ====================
    public static EnrollmentDetailDTO toDetailDTO(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        User user = enrollment.getUser();

        return EnrollmentDetailDTO.builder()
                .id(enrollment.getId())
                // User info (CHO ADMIN)
                .userId(user.getId())
                .username(user.getDisplayName())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                // Course info
                .courseId(course.getId())
                .courseName(course.getCourseName())
                .courseDescription(course.getCourseDescription())
                .courseThumbnail(course.getCourseImageUrl())
                .level(course.getCourseLevel())
                .enrollDate(enrollment.getEnrollDate())
                .expiryDate(enrollment.getExpiryDate())
                .lastAccessed(enrollment.getLastAccessed())
                .progress(enrollment.getProgress())
                .status(enrollment.getStatus().name())
                .completedLessons(enrollment.getCompletedLessons())
                .totalLessons(calculateTotalLessons(course))
                .isExpired(enrollment.isExpired())
                // Admin fields
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }

    // ==================== HELPER METHOD ====================
    private static Integer calculateTotalLessons(Course course) {
        if (course.getSections() == null) return 0;
        return course.getSections().stream()
                .mapToInt(section -> section.getLessons() != null ? section.getLessons().size() : 0)
                .sum();
    }
}


