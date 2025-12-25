package korastudy.be.repository;

import korastudy.be.entity.Course.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findBySectionIdOrderByOrderIndex(Long sectionId);

    @Query("SELECT l FROM Lesson l WHERE l.section.course.id = :courseId ORDER BY l.section.orderIndex, l.orderIndex")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);

    Optional<Lesson> findByIdAndSectionCourseId(Long lessonId, Long courseId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.id = :sectionId")
    long countBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT MAX(l.orderIndex) FROM Lesson l WHERE l.section.id = :sectionId")
    Integer findMaxOrderIndexBySectionId(@Param("sectionId") Long sectionId);

    boolean existsBySectionIdAndOrderIndex(Long sectionId, Integer orderIndex);

    @Query("SELECT l FROM Lesson l " +
            "JOIN l.section s " +
            "JOIN s.course c " +
            "WHERE c.id = :courseId " +
            "ORDER BY s.orderIndex ASC, l.orderIndex ASC")
    List<Lesson> findAllByCourseId(@Param("courseId") Long courseId);
}