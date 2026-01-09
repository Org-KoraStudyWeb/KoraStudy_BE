package korastudy.be.service.impl;

import korastudy.be.dto.request.enrollment.EnrollmentRequest;
import korastudy.be.dto.response.enrollment.EnrollmentDTO;
import korastudy.be.dto.response.enrollment.EnrollmentDetailDTO;
import korastudy.be.dto.response.enrollment.EnrollmentStatsDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.Enum.EnrollmentStatus;
import korastudy.be.entity.User.User;
import korastudy.be.exception.PaymentException;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.mapper.EnrollmentMapper;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.EnrollmentRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService implements IEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EnrollmentDTO enrollUserToCourse(EnrollmentRequest request, String username) {
        // 1. Tìm course
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        // 2. Kiểm tra khóa học có phí
        if (course.getCoursePrice() > 0) {
            throw new PaymentException("Khóa học này yêu cầu thanh toán");
        }

        // 3. Tìm user bằng username từ token
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        // 4. Kiểm tra user đã enrolled chưa
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new RuntimeException("Bạn đã đăng ký khóa học này rồi");
        }

        // 5. Tạo enrollment với đầy đủ thông tin
        Enrollment enrollment = EnrollmentMapper.toEntity(request, user, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return EnrollmentMapper.toDTO(savedEnrollment);
    }

    @Override
    public List<EnrollmentDTO> getUserEnrollments(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByLastAccessedDesc(userId);
        return enrollments.stream().map(EnrollmentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getUserEnrollmentsByStatus(Long userId, EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, status);
        return enrollments.stream().map(EnrollmentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean isUserEnrolledInCourse(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Page<EnrollmentDetailDTO> getCourseEnrollments(Long courseId, Pageable pageable) {
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);
        return enrollmentPage.map(EnrollmentMapper::toDetailDTO); //
    }

    @Override
    public long countEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    @Override
    @Transactional
    public EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, double progress) {
        // Validate progress
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Tiến độ phải nằm trong khoảng 0-100");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký"));

        enrollment.setProgress(progress);
        enrollment.setLastAccessed(LocalDateTime.now());

        // Tự động chuyển status nếu hoàn thành
        if (progress >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        }

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toDTO(updatedEnrollment);
    }

    @Override
    @Transactional
    public void updateProgressFromLessons(Long userId, Long courseId, int completedLessons) {
        enrollmentRepository.findByUserIdAndCourseId(userId, courseId).ifPresent(enrollment -> {
            Course course = enrollment.getCourse();
            int totalLessons = calculateTotalLessons(course);
            double newProgress = totalLessons > 0 ? (double) completedLessons / totalLessons * 100 : 0;

            enrollment.setProgress(newProgress);
            enrollment.setCompletedLessons(completedLessons);
            enrollment.setLastAccessed(LocalDateTime.now());

            if (newProgress >= 100 && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
            }

            enrollmentRepository.save(enrollment);
        });
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký"));

        // Thay vì xóa, chuyển status thành CANCELLED
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    @Override
    public EnrollmentDTO getEnrollmentById(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký"));
        return EnrollmentMapper.toDTO(enrollment);
    }

    @Override
    public EnrollmentStatsDTO getEnrollmentStats() {
        return EnrollmentStatsDTO.builder()
                .totalEnrollments(enrollmentRepository.count())
                .activeEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE))
                .completedEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED))
                .build();
    }

    @Override
    public List<korastudy.be.dto.response.enrollment.RecentEnrollmentDTO> getRecentEnrollments(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Enrollment> enrollments = enrollmentRepository.findRecentEnrollments(pageable);
        return enrollments.stream().map(enrollment -> korastudy.be.dto.response.enrollment.RecentEnrollmentDTO.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .userFullName(enrollment.getUser().getFirstName() + " " + enrollment.getUser().getLastName())
                .userAvatar(enrollment.getUser().getAvatar())
                .userEmail(enrollment.getUser().getEmail())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getCourseName())
                .enrollmentDate(enrollment.getCreatedAt())
                .build())
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private int calculateTotalLessons(Course course) {
        if (course.getSections() == null) return 0;
        return course.getSections().stream().mapToInt(section -> section.getLessons() != null ? section.getLessons().size() : 0).sum();
    }
}