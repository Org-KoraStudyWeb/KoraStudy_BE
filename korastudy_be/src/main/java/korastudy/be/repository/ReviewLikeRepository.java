// ReviewLikeRepository.java
package korastudy.be.repository;

import korastudy.be.entity.Review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    long countByReviewId(Long reviewId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    @Query("SELECT COUNT(l) FROM ReviewLike l WHERE l.review.id = :reviewId")
    Long countLikesByReviewId(@Param("reviewId") Long reviewId);

    void deleteByReviewId(Long reviewId);

    // SỬA LẠI: Thêm @Query để chỉ định rõ ràng
    @Query("SELECT rl.user.id FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    List<Long> findUserIdsByReviewId(@Param("reviewId") Long reviewId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}