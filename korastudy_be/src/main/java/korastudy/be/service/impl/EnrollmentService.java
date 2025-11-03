package korastudy.be.service.impl;

import korastudy.be.dto.request.course.EnrollmentRequest;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.EnrollmentRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService implements IEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public EnrollmentDTO enrollUserToCourse(EnrollmentRequest request) {
        // Kiểm tra xem đã đăng ký hay chưa
        if (enrollmentRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new IllegalStateException("Người dùng đã đăng ký khóa học này");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .enrollDate(LocalDate.now())
                .progress(0.0)
                .build();

        // Nếu khóa học không miễn phí, đặt ngày hết hạn là 1 năm sau
        if (!course.isFree()) {
            enrollment.setExpiryDate(LocalDate.now().plusYears(1));
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return mapToDTO(savedEnrollment);
    }

    @Override
    public List<EnrollmentDTO> getUserEnrollments(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return enrollments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getCourseEnrollments(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelEnrollment(Long enrollmentId) {
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new ResourceNotFoundException("Không tìm thấy đăng ký khóa học với ID: " + enrollmentId);
        }
        enrollmentRepository.deleteById(enrollmentId);
    }

    @Override
    public EnrollmentDTO getEnrollmentById(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học với ID: " + enrollmentId));
        return mapToDTO(enrollment);
    }

    @Override
    public EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, double progress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học với ID: " + enrollmentId));
                
        enrollment.setProgress(progress);
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return mapToDTO(updatedEnrollment);
    }

    @Override
    public EnrollmentDTO mapToDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .username(enrollment.getUser().getUsername())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getCourseName())
                .enrollDate(enrollment.getEnrollDate())
                .expiryDate(enrollment.getExpiryDate())
                .progress(enrollment.getProgress())
                .build();
    }

    @Override
    public boolean isUserEnrolledInCourse(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }
    
    @Override
    public List<EnrollmentDTO> getCourseEnrollmentsWithPagination(Long courseId, Pageable pageable) {
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);
        return enrollmentPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
    
    @Override
    public long countTotalEnrollments() {
        return enrollmentRepository.count();
    }
}
