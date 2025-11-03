package korastudy.be.repository;

import korastudy.be.entity.Course.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByCourseId(Long courseId);
    
    Page<Review> findByCourseId(Long courseId, Pageable pageable);
    
    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(Long courseId);
    
    long countByCourseId(Long courseId);
}
