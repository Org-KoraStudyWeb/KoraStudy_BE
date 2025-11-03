package korastudy.be.repository;

import korastudy.be.entity.Course.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findBySectionId(Long sectionId);
    
    List<Lesson> findBySectionIdOrderByOrderIndex(Long sectionId);
}
