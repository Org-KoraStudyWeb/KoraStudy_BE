package korastudy.be.service.impl;

import korastudy.be.dto.request.course.CourseCreateRequest;
import korastudy.be.dto.request.course.CourseUpdateRequest;
import korastudy.be.dto.response.course.CourseDTO;
import korastudy.be.dto.response.course.SectionDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.EnrollmentRepository;
import korastudy.be.repository.ReviewRepository;
import korastudy.be.service.ICourseService;
import korastudy.be.service.ISectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {

    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ISectionService sectionService;

    @Override
    public CourseDTO createCourse(CourseCreateRequest request) {
        Course course = Course.builder()
                .courseName(request.getCourseName())
                .courseDescription(request.getCourseDescription())
                .courseImageUrl(request.getCourseImageUrl())
                .courseLevel(request.getCourseLevel())
                .coursePrice(request.getCoursePrice())
                .isFree(request.isFree())
                .isPublished(request.isPublished())
                .viewCount(0L)
                .createdAt(LocalDateTime.now())
                .lastModified(LocalDateTime.now())
                .build();
        
        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse);
    }

    @Override
    public CourseDTO updateCourse(Long courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));
        
        course.setCourseName(request.getCourseName());
        course.setCourseDescription(request.getCourseDescription());
        course.setCourseImageUrl(request.getCourseImageUrl());
        course.setCourseLevel(request.getCourseLevel());
        course.setCoursePrice(request.getCoursePrice());
        course.setFree(request.isFree());
        course.setPublished(request.isPublished());
        course.setLastModified(LocalDateTime.now());
        
        Course updatedCourse = courseRepository.save(course);
        return mapToDTO(updatedCourse);
    }

    @Override
    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));
        return mapToDTO(course);
    }

    @Override
    public List<CourseDTO> getAllCourses(boolean publishedOnly) {
        List<Course> courses;
        if (publishedOnly) {
            courses = courseRepository.findByIsPublishedTrue();
        } else {
            courses = courseRepository.findAll();
        }
        return courses.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }

    @Override
    public CourseDTO publishCourse(Long courseId, boolean isPublished) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));
        
        course.setPublished(isPublished);
        course.setLastModified(LocalDateTime.now());
        
        Course updatedCourse = courseRepository.save(course);
        return mapToDTO(updatedCourse);
    }

    @Override
    public CourseDTO incrementViewCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));
        
        course.setViewCount(course.getViewCount() + 1);
        Course updatedCourse = courseRepository.save(course);
        return mapToDTO(updatedCourse);
    }

    @Override
    public List<CourseDTO> searchCourses(String keyword) {
        List<Course> courses = courseRepository.findByCourseNameContainingOrCourseDescriptionContaining(keyword, keyword);
        return courses.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDTO mapToDTO(Course course) {
        // Tính toán số lượng đăng ký
        int enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
        
        // Tính toán đánh giá trung bình
        Double avgRating = reviewRepository.findAverageRatingByCourseId(course.getId());
        double averageRating = (avgRating != null) ? avgRating : 0.0;
        
        // Lấy danh sách sections
        List<SectionDTO> sectionDTOs = course.getSections() != null ?
                course.getSections().stream()
                        .map(sectionService::mapToDTO)
                        .collect(Collectors.toList()) : 
                List.of();
                
        return CourseDTO.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .courseDescription(course.getCourseDescription())
                .courseImageUrl(course.getCourseImageUrl())
                .courseLevel(course.getCourseLevel())
                .coursePrice(course.getCoursePrice())
                .isFree(course.isFree())
                .isPublished(course.isPublished())
                .viewCount(course.getViewCount())
                .createdAt(course.getCreatedAt())
                .lastModified(course.getLastModified())
                .sections(sectionDTOs)
                .averageRating(averageRating)
                .enrollmentCount(enrollmentCount)
                .build();
    }
    
    @Override
    public List<CourseDTO> getAllCoursesWithPagination(Pageable pageable) {
        Page<Course> coursePage = courseRepository.findAll(pageable);
        return coursePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CourseDTO> searchCoursesWithPagination(String keyword, Pageable pageable) {
        Page<Course> coursePage = courseRepository.findByCourseNameContainingOrCourseDescriptionContaining(
                keyword, keyword, pageable);
        return coursePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countCourses() {
        return courseRepository.count();
    }
    
    @Override
    public long countPublishedCourses() {
        return courseRepository.countByIsPublishedTrue();
    }
    
    @Override
    public long countUnpublishedCourses() {
        return courseRepository.countByIsPublishedFalse();
    }
    
    @Override
    public long countSearchResults(String keyword) {
        return courseRepository.countByCourseNameContainingOrCourseDescriptionContaining(keyword, keyword);
    }
}
