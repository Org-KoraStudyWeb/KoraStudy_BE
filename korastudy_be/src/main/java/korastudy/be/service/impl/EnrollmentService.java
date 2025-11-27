package korastudy.be.service.impl;

import korastudy.be.dto.request.course.EnrollmentRequest;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
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
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        // 2. Kiểm tra khóa học có phí
        if (course.getCoursePrice() > 0) {
            throw new PaymentException("Khóa học này yêu cầu thanh toán. Vui lòng thanh toán trước khi ghi danh.");
        }

        // 3. ✅ Tìm user bằng USERNAME từ token (không phải từ request)
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));

        // 4. Kiểm tra user đã enrolled chưa
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId());
        if (alreadyEnrolled) {
            throw new RuntimeException("Bạn đã đăng ký khóa học này rồi");
        }

        // 5. Tạo enrollment
        Enrollment enrollment = Enrollment.builder().user(user) // ✅ Dùng user từ database
                .course(course).progress(0.0).enrollDate(LocalDate.now()).expiryDate(LocalDate.now().plusMonths(6)) // 6 tháng
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return mapToDTO(savedEnrollment);
    }

    @Override
    public List<EnrollmentDTO> getUserEnrollments(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return enrollments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getCourseEnrollments(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học với ID: " + enrollmentId));
        enrollmentRepository.delete(enrollment);
    }

    @Override
    public EnrollmentDTO getEnrollmentById(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học với ID: " + enrollmentId));
        return mapToDTO(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, double progress) {
        // Validate progress
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Tiến độ phải nằm trong khoảng 0-100");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học với ID: " + enrollmentId));

        enrollment.setProgress(progress);
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        return mapToDTO(updatedEnrollment);
    }

    @Override
    public EnrollmentDTO mapToDTO(Enrollment enrollment) {
        return EnrollmentMapper.toDTO(enrollment);
    }

    @Override
    public boolean isUserEnrolledInCourse(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public List<EnrollmentDTO> getCourseEnrollmentsWithPagination(Long courseId, Pageable pageable) {
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);
        return enrollmentPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
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