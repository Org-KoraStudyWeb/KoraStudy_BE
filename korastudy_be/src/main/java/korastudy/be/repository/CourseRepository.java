package korastudy.be.repository;

import korastudy.be.entity.Course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByIsPublishedTrue();

    Optional<Course> findByIdAndIsPublishedTrue(Long id);

    boolean existsByName(String name);

    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);
}
