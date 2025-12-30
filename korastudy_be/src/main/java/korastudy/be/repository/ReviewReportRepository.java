// ReviewReportRepository.java
package korastudy.be.repository;

import korastudy.be.entity.Review.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    Optional<ReviewReport> findByReviewIdAndUserId(Long reviewId, Long userId);

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    long countByReviewId(Long reviewId);

    long countByReviewIdAndResolvedFalse(Long reviewId);

    void deleteByReviewId(Long reviewId);

    @Query("SELECT COUNT(r) FROM ReviewReport r WHERE r.review.id = :reviewId AND r.resolved = false")
    Long countActiveReportsByReviewId(@Param("reviewId") Long reviewId);

    List<ReviewReport> findAllByReviewId(Long reviewId);
}