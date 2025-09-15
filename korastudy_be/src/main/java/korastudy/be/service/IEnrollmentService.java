package korastudy.be.service;

import korastudy.be.dto.request.course.EnrollmentRequest;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.entity.Course.Enrollment;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEnrollmentService {
    
    EnrollmentDTO enrollUserToCourse(EnrollmentRequest request);
    
    List<EnrollmentDTO> getUserEnrollments(Long userId);
    
    List<EnrollmentDTO> getCourseEnrollments(Long courseId);
    
    void cancelEnrollment(Long enrollmentId);
    
    EnrollmentDTO getEnrollmentById(Long enrollmentId);
    
    EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, double progress);
    
    EnrollmentDTO mapToDTO(Enrollment enrollment);
    
    boolean isUserEnrolledInCourse(Long userId, Long courseId);
    
    // Methods for admin panel with pagination
    List<EnrollmentDTO> getCourseEnrollmentsWithPagination(Long courseId, Pageable pageable);
    
    long countEnrollmentsByCourseId(Long courseId);
    
    long countTotalEnrollments();
}
