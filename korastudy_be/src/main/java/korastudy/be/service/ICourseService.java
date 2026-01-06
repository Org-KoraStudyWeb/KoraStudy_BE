package korastudy.be.service;

import korastudy.be.dto.request.course.CourseCreateRequest;
import korastudy.be.dto.request.course.CourseUpdateRequest;
import korastudy.be.dto.response.course.CourseDTO;
import korastudy.be.entity.Course.Course;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ICourseService {

    CourseDTO createCourse(CourseCreateRequest request);

    CourseDTO updateCourse(Long courseId, CourseUpdateRequest request);

    CourseDTO getCourseById(Long courseId);

    List<CourseDTO> getAllCourses(boolean publishedOnly);

    void deleteCourse(Long courseId);

    CourseDTO publishCourse(Long courseId, boolean isPublished);

    CourseDTO incrementViewCount(Long courseId);

    List<CourseDTO> searchCourses(String keyword);

    List<CourseDTO> searchCoursesAdvanced(String keyword, String level, Double minPrice, Double maxPrice, Boolean isPublished, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    CourseDTO mapToDTO(Course course);

    // Methods for admin panel with pagination
    List<CourseDTO> getAllCoursesWithPagination(Pageable pageable);

    List<CourseDTO> searchCoursesWithPagination(String keyword, Pageable pageable);

    long countCourses();

    long countPublishedCourses();

    long countUnpublishedCourses();

    long countSearchResults(String keyword);
}
