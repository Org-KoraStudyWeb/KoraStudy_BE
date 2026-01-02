package korastudy.be.repository;

import korastudy.be.entity.Course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("""
            SELECT c FROM Course c
            WHERE 
                (:keyword IS NULL OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                    OR LOWER(c.courseDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:level IS NULL OR c.courseLevel = :level)
            AND (:minPrice IS NULL OR c.coursePrice >= :minPrice)
            AND (:maxPrice IS NULL OR c.coursePrice <= :maxPrice)
            AND (:isPublished IS NULL OR c.isPublished = :isPublished)
            AND (:startDate IS NULL OR c.createdAt >= :startDate)
            AND (:endDate IS NULL OR c.createdAt <= :endDate)
            """)
    Page<Course> searchCoursesAdvanced(@Param("keyword") String keyword, @Param("level") String level, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, @Param("isPublished") Boolean isPublished, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}
