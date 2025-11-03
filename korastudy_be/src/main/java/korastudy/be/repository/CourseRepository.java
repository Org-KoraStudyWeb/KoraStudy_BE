package korastudy.be.repository;

import korastudy.be.entity.Course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByIsPublishedTrue();
    
    List<Course> findByCourseNameContainingOrCourseDescriptionContaining(String keyword1, String keyword2);
    
    Page<Course> findAll(Pageable pageable);
    
    Page<Course> findByCourseNameContainingOrCourseDescriptionContaining(String keyword1, String keyword2, Pageable pageable);
    
    long countByIsPublishedTrue();
    
    long countByIsPublishedFalse();
    
    long countByCourseNameContainingOrCourseDescriptionContaining(String keyword1, String keyword2);
}
