package korastudy.be.mapper;

import korastudy.be.dto.request.course.EnrollmentRequest;
import korastudy.be.dto.response.course.EnrollmentDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.User.User;

import java.time.LocalDate;

public class EnrollmentMapper {

    public static Enrollment toEntity(EnrollmentRequest request, User user, Course course) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollDate(LocalDate.now());
        enrollment.setExpiryDate(LocalDate.now().plusMonths(6));
        enrollment.setProgress(0.0);
        return enrollment;
    }

    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        return new EnrollmentDTO(
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getUser().getDisplayName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getCourseName(),
                enrollment.getEnrollDate(),
                enrollment.getExpiryDate(),
                enrollment.getProgress()
        );
    }
}

